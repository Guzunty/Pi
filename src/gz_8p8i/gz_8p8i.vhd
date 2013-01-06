----------------------------------------------------------------------------------
-- Company:        Guzunty
-- Engineer:       campbellsan
-- 
-- Create Date:    18:13:32 01/05/2013 
-- Design Name:    Guzunty 8 pwms 8 inputs
-- Module Name:    gz_8p8i - RTL 
-- Project Name:   gz_8p8i
-- Target Devices: XC9572-PC44
-- Tool versions:  ISE 14.3
-- Description:    Provides 8 pulse width modulation outputs with 8 inputs
--                 
-- Dependencies:   None
--
-- Revision:       1.0
-- Revision 0.01 - File Created
-- Additional Comments: 
--
----------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.NUMERIC_STD.ALL;

entity gz_8p8i is
    Port ( pwms : out  STD_LOGIC_VECTOR (7 downto 0);
           inputs : in  STD_LOGIC_VECTOR (7 downto 0);
           mosi : in  STD_LOGIC;
           miso : out  STD_LOGIC;
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
           clk : in  STD_LOGIC);
end gz_8p8i;

architecture RTL of gz_8p8i is
signal bit_cnt: std_logic_vector (2 downto 0) := (others => '1');
type pwm_counter_t is array (7 downto 0) of std_logic_vector(4 downto 0);
signal pwm_counters: pwm_counter_t;
begin
  process (sclk, sel) is
    variable next_bit: std_logic_vector(2 downto 0);
	 variable pwm_selector: std_logic_vector(2 downto 0);
	 variable loading_val: std_logic := '0';
  begin
	 if (sel = '0') then
	   if (rising_edge(sclk)) then
	     if (loading_val = '0') then
		    if (bit_cnt = "010" or bit_cnt = "001" or bit_cnt = "000") then
            pwm_selector(to_integer(unsigned(bit_cnt))) := mosi;
			 end if;
   	  else -- loading_val = 1 loading counter value;
		    if (bit_cnt /= "111" and bit_cnt /= "110" and bit_cnt /= "101") then
		      pwm_counters(to_integer(unsigned(pwm_selector)))(to_integer(unsigned(bit_cnt))) <= mosi;
			 end if;
		  end if;
	   end if;
		if (falling_edge(sclk)) then
	     if (bit_cnt = "000") then
		    next_bit := (others => '1');
			 loading_val := not loading_val;
		  else
          next_bit := std_logic_vector(unsigned(bit_cnt) - 1);
		  end if;
		  bit_cnt <= next_bit;
      end if;
    else
	   next_bit := "XXX";
		pwm_selector := "XXX";
	   bit_cnt <= (others => '1');
		loading_val := '0';
    end if;
  end process;
	
  process (sel, bit_cnt) is
  begin
	 if (sel = '0') then
		miso <= inputs(to_integer(unsigned(bit_cnt)));
	 else 
      miso <= 'Z';
	 end if;
  end process;
	
  process (clk) is
	 variable main_counter: std_logic_vector(4 downto 0):= (others => '1');
  begin
	 if (rising_edge(clk)) then
	   if (main_counter /= "11111") then
	     main_counter := std_logic_vector(unsigned(main_counter) + 1);
	   else
	     main_counter := (others => '0');
		  pwms <= (others => '1');
	   end if;
	 end if;
    for I in 0 to 7 loop
      if (main_counter = pwm_counters(I)) then
	     pwms(I) <= '0';
	   end if;
	 end loop;
  end process;
end RTL;

