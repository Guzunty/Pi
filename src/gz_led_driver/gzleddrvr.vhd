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
			  digit_enas : out  std_logic_vector (3 downto 0) := "0000";
			  segments: out std_logic_vector (6 downto 0);
			  inputs : in std_logic_vector (5 downto 0));
end gzleddrvr;

architecture behavioral of gzleddrvr is
signal bit_cnt : integer range 0 to 8 := 0;
type display_t is array (0 to 3) of std_logic_vector (6 downto 0);
signal display: display_t;
begin
   process (sclk, sel) is
	variable digit_cnt: integer range 0 to 3 := 0;
	variable next_bit: integer range 0 to 8;
	begin
	  if (sel = '0') then
	      if (rising_edge(sclk)) then
			  -- reverse the bit order and ignore the top bit
			  if (bit_cnt /= 0) then
             display(digit_cnt)(7 - bit_cnt) <= mosi;
			  end if;
	      end if;
			if (falling_edge(sclk)) then
           next_bit := bit_cnt + 1;
	        if (next_bit = 8) then
			    next_bit := 0;
				 if (digit_cnt + 1 = 4) then
				   digit_cnt := 0;
				 else
		         digit_cnt := digit_cnt + 1;
				 end if;
		     end if;
			  bit_cnt <= next_bit;
         end if;
     else
       digit_cnt := 0;
		 bit_cnt <= 0;
     end if;
	end process;
	
   process (sel, bit_cnt) is
	begin
	  if (sel = '0') then
	    if (bit_cnt < 6) then
		   miso <= inputs(bit_cnt);
		 else
		   miso <= '0';
		 end if;
	  else 
       miso <= 'Z';
	  end if;
	end process;
	
	process (clk) is
	variable cur_digit: std_logic_vector (1 downto 0) := "00";
	begin
     if (rising_edge(clk)) then
		 if (cur_digit = "00") then
		   cur_digit := "01";
		   digit_enas <= "0001";
			segments <= display(0);
		 else 
		   if (cur_digit = "01") then
		     cur_digit := "10";
		     digit_enas <= "0010";
			  segments <= display(1);
		   else
			  if (cur_digit = "10") then
		       cur_digit := "11";
		       digit_enas <= "0100";
			    segments <= display(2);
		     else
			    if (cur_digit = "11") then
		         cur_digit := "00";
		         digit_enas <= "1000";
			      segments <= display(3);
				 end if;
			  end if;
			end if;
		 end if;
     end if;
	end process;
end behavioral;