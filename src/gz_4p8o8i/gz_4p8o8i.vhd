----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    12:14:29 01/14/2013 
-- Design Name: 
-- Module Name:    gz_4p8o8i - Behavioral 
-- Project Name: 
-- Target Devices: 
-- Tool versions: 
-- Description: 
--
-- Dependencies: 
--
-- Revision: 
-- Revision 0.01 - File Created
-- Additional Comments: 
--
----------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.NUMERIC_STD.ALL;

-- Uncomment the following library declaration if instantiating
-- any Xilinx primitives in this code.
--library UNISIM;
--use UNISIM.VComponents.all;

entity gz_4p8o8i is
    Port ( pwms : out  STD_LOGIC_VECTOR (3 downto 0);
           outputs : out  STD_LOGIC_VECTOR (7 downto 0) := (others => '0');
           inputs : in  STD_LOGIC_VECTOR (7 downto 0);
           mosi : in  STD_LOGIC;
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
           miso : out  STD_LOGIC;
           clk : in  STD_LOGIC);
end gz_4p8o8i;

architecture Behavioral of gz_4p8o8i is
signal bit_cnt: natural range 0 to 7 := 7;
type pwm_counter_t is array (3 downto 0) of std_logic_vector(7 downto 0);
signal pwm_counters: pwm_counter_t := (others=> (others=> '0'));
begin
  process (sclk, sel) is
    variable next_bit: natural range 0 to 7;
	 variable pwm_selector: std_logic_vector(2 downto 0);
	 variable loading_val: std_logic := '0';
  begin
	 if (sel = '0') then
	   if (rising_edge(sclk)) then
	     if (loading_val = '0') then
		    if (bit_cnt = 2 or bit_cnt = 1 or bit_cnt = 0) then
            pwm_selector(bit_cnt) := mosi;
			 end if;
   	  else -- loading_val = 1 loading counter value;
		    if (pwm_selector = "100") then
			   outputs(bit_cnt) <= mosi;
			 else
			   if (pwm_selector /= "101" and pwm_selector /= "110" and pwm_selector /= "111") then
			     -- we only have 4 pwms
	           pwm_counters(to_integer(unsigned(pwm_selector)))(bit_cnt) <= mosi;
				end if;
			 end if;
		  end if;
	   end if;
		if (falling_edge(sclk)) then
	     if (bit_cnt = 0) then
		    next_bit := 7;
			 loading_val := not loading_val;
		  else
          next_bit := bit_cnt - 1;
		  end if;
		  bit_cnt <= next_bit;
      end if;
    else
		pwm_selector := "000";
	   bit_cnt <= 7;
		loading_val := '0';
    end if;
  end process;
	
  process (sel, bit_cnt) is
  begin
	 if (sel = '0') then
		miso <= inputs(bit_cnt);
	 else 
      miso <= 'Z';
	 end if;
  end process;
	
  process (clk) is
	 variable main_counter: std_logic_vector(7 downto 0):= (others => '0');
  begin
	 if (rising_edge(clk)) then
	   if (main_counter /= "11111111") then
	     main_counter := std_logic_vector(unsigned(main_counter) + 1);
	   else
	     main_counter := (others => '0');
		  pwms <= (others => '1');
	   end if;
	 end if;
    for I in 0 to 3 loop
      if (main_counter = pwm_counters(I)) then
	     pwms(I) <= '0';
	   end if;
	 end loop;
  end process;

end Behavioral;

