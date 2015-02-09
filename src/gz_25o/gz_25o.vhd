----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    14:24:13 01/06/2013 
-- Design Name: 
-- Module Name:    gz_25o - Behavioral 
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

entity gz_25o is
    Port ( outputs : out  STD_LOGIC_VECTOR (24 downto 0);
           mosi : in  STD_LOGIC;
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
           miso : out  STD_LOGIC);
end gz_25o;

architecture RTL of gz_25o is
signal bit_cnt: natural range 7 downto 0 := 7;
signal byte_cnt: natural range 3 downto 0 := 0;
begin
  process (sclk, sel) is
  begin
	 if sel = '0' then
	   if rising_edge(sclk) then
		  if byte_cnt < 3 then
          outputs((byte_cnt * 8) + bit_cnt) <= mosi;
		  else -- byte_cnt is 3
		    if byte_cnt = 3 and bit_cnt = 0 then
            outputs(24) <= mosi;
			 end if;
		  end if;
      end if;
		if falling_edge(sclk) then
	     if (bit_cnt = 0) then
		    bit_cnt <= 7;
			 byte_cnt <= byte_cnt + 1;
        else
		    bit_cnt <= bit_cnt - 1;
        end if;
      end if;
		miso <= '0';
    else
	   bit_cnt <= 7;
		byte_cnt <= 0;
		miso <= 'Z';
    end if;
  end process;

end RTL;

