----------------------------------------------------------------------------------
-- Company:        Guzunty
-- Engineer:       campbellsan
-- 
-- Create Date:    19:09:07 01/04/2013 
-- Design Name:    Guzunty 16 out 8 in
-- Module Name:    gz_16o8i - RTL 
-- Project Name:   gz_16o8i
-- Target Devices: XC9572XL-PC44
-- Tool versions:  ISE 14.3
-- Description:    Provides general IO, 16 outs and 8 ins
--
-- Dependencies:   None. 
--
-- Revision: 1.0
-- Revision 0.01 - File Created
-- Additional Comments: 
--
----------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.NUMERIC_STD.ALL;

entity gz_16o8i is
    Port ( outputs : out  STD_LOGIC_VECTOR (15 downto 0);
           inputs : in  STD_LOGIC_VECTOR (7 downto 0);
           mosi : in  STD_LOGIC;
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
           miso : out  STD_LOGIC);
end gz_16o8i;

architecture RTL of gz_16o8i is
signal bit_cnt: std_logic_vector (2 downto 0) := (others => '1');
begin
  process (sclk, sel) is
    variable next_bit: std_logic_vector(2 downto 0);
	 variable pwm_selector: std_logic_vector(2 downto 0);
	 variable hi_byte: std_logic := '0';
  begin
	 if (sel = '0') then
	   if (rising_edge(sclk)) then
	     if (hi_byte = '0') then
          outputs(to_integer(unsigned(bit_cnt))) <= mosi;
   	  else -- loading upper byte of set
          outputs(8 + to_integer(unsigned(bit_cnt))) <= mosi;
		  end if;
	   end if;
		if (falling_edge(sclk)) then
	     if (bit_cnt = "000") then
		    next_bit := (others => '1');
			 hi_byte := not hi_byte;
		  else
          next_bit := std_logic_vector(unsigned(bit_cnt) - 1);
		  end if;
		  bit_cnt <= next_bit;
      end if;
    else
	   bit_cnt <= (others => '1');
		hi_byte := '0';
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

end RTL;

