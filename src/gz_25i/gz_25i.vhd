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
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
           miso : out  STD_LOGIC);
end gz_25i;

architecture RTL of gz_25i is
signal bit_cnt: natural range 7 downto 0 := 7;
signal byte_cnt: natural range 3 downto 0 := 0;
begin
  process (sclk, sel, byte_cnt, bit_cnt, inputs) is
  begin
	 if sel = '0' then
		if byte_cnt < 3 then
        miso <= inputs((byte_cnt * 8) + bit_cnt);
      else -- byte_cnt is 3
        if byte_cnt = 3 and bit_cnt = 0 then
          miso <= inputs(24);
        else
          miso <= '0';
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
    else
	   bit_cnt <= 7;
		byte_cnt <= 0;
		miso <= 'Z';
    end if;
  end process;

end RTL;