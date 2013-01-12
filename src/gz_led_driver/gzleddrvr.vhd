----------------------------------------------------------------------------------
-- Company:        Guzunty
-- Engineer:       campbellsan
-- 
-- Create Date:    09:29:30 12/08/2012 
-- Design Name:    1606i
-- Module Name:    gzleddrvr - Behavioral 
-- Project Name:   gzleddrvr
-- Target Devices: Guzunty Pi XC9572XL-PC44
-- Tool versions:  ISE 14.3
-- Description:    Provides 4 digit 7 segment LED driver via SPI + 6 inputs
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

entity gzleddrvr is
    Port ( mosi : in  STD_LOGIC;
           miso : out  STD_LOGIC;
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
           clk : in  STD_LOGIC;
			  digit_enas : inout  std_logic_vector (3 downto 0) := "0001";
			  segments: out std_logic_vector (7 downto 0);
			  inputs : in std_logic_vector (7 downto 0));
end gzleddrvr;

architecture behavioral of gzleddrvr is
signal bit_cnt : integer range 0 to 7 := 7;
type display_t is array (0 to 3) of std_logic_vector (7 downto 0);
signal display: display_t := ((others=> (others=> '0')));
begin
   process (sclk, sel) is
	variable digit_cnt: integer range 0 to 3 := 0;
	variable next_bit: integer range 0 to 7 := 7;
	begin
	  if (sel = '0') then
	      if (rising_edge(sclk)) then
           display(digit_cnt)(bit_cnt) <= mosi;
	      end if;
			if (falling_edge(sclk)) then
	        if (bit_cnt = 0) then
			    next_bit := 7;
				 if (digit_cnt = 3) then
				   digit_cnt := 0;
				 else
		         digit_cnt := digit_cnt + 1;
				 end if;
			  else
             next_bit := bit_cnt - 1;
		     end if;
			  bit_cnt <= next_bit;
         end if;
     else
       digit_cnt := 0;
		 bit_cnt <= 7;
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
	variable cur_digit: natural range 0 to 3 := 0;
	begin
     if (rising_edge(clk)) then
		 digit_enas <= digit_enas(2 downto 0) & digit_enas(3); 
		 if (cur_digit = 3) then
		   cur_digit := 0;
		 else
		   cur_digit := cur_digit + 1;
		 end if;
		 segments <= display(cur_digit);
	  end if;
	end process;

end behavioral;