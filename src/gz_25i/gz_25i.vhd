----------------------------------------------------------------------------------
-- Company:        Guzunty
-- Engineer:       campbellsan
-- 
-- Create Date:    13:39:48 01/13/2013 
-- Design Name:    Guzunty 25 in
-- Module Name:    gz_25i - RTL 
-- Project Name:   gz_25i
-- Target Devices: XC9572XL-PC44
-- Tool versions:  ISE 14.3
-- Description:    Provides general IO, 25 ins
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

entity gz_25i is
    Port ( inputs : in  STD_LOGIC_VECTOR (24 downto 0);
           mosi : in  STD_LOGIC;
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
           miso : out  STD_LOGIC);
end gz_25i;

architecture RTL of gz_25i is
signal bit_cnt: natural range 0 to 7 := 7;
signal byte_cnt: natural range 0 to 3 := 0;
begin
  process (sclk, sel) is
    variable next_bit: natural range 0 to 7;
  begin
	 if (sel = '0') then
		if (falling_edge(sclk)) then
	     if (bit_cnt = 0) then
		    next_bit := 7;
			 if (byte_cnt = 3) then
			   byte_cnt <= 0;
			 else
			   byte_cnt <= byte_cnt + 1;
			 end if;
		  else
          next_bit := bit_cnt - 1;
		  end if;
		  bit_cnt <= next_bit;
      end if;
    else
	   bit_cnt <= 7;
		byte_cnt <= 0;
    end if;
  end process;
	
  process (sel, bit_cnt) is
  begin
	 if (sel = '0') then
	   if (byte_cnt = 0) then
	     miso <= inputs(bit_cnt);
		else
	     if (byte_cnt = 1) then
	       miso <= inputs(8 + bit_cnt);
		  else
	       if (byte_cnt = 2) then
	         miso <= inputs(16 + bit_cnt);
			 else
			   if (byte_cnt = 3) then
				  if (bit_cnt = 0) then
	             miso <= inputs(24);
				  else
				    miso <= '0';
				  end if;
				end if;
		    end if;
		  end if;
		end if;
	 else 
      miso <= 'Z';
	 end if;
  end process;
end RTL;