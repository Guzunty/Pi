----------------------------------------------------------------------------------
-- Company:        Guzunty
-- Engineer:       campbellsan
-- 
-- Create Date:    09:57:16 01/09/2013 
-- Design Name:    Guzunty 24 in 1 direct out
-- Module Name:    gz_24i1O - RTL 
-- Project Name:   gz_24i1O
-- Target Devices: XC9572XL-PC44
-- Tool versions:  ISE 14.3
-- Description:    Provides general IO, 24 ins and 1 out
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

entity gz_24i1O is
    Port ( inputs : in  STD_LOGIC_VECTOR (23 downto 0);
           output : out  STD_LOGIC;
           mosi : in  STD_LOGIC;
           miso : out  STD_LOGIC;
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
           p_out : in  STD_LOGIC);
end gz_24i1O;

architecture RTL of gz_24i1O is
signal bit_cnt: std_logic_vector (2 downto 0) := (others => '1');
signal byte_cnt: std_logic_vector(1 downto 0) := "00";
begin
  process (sclk, sel) is
    variable next_bit: std_logic_vector(2 downto 0);
  begin
	 if (sel = '0') then
		if (falling_edge(sclk)) then
	     if (bit_cnt = "000") then
		    next_bit := (others => '1');
			 if (byte_cnt = "00") then
			   byte_cnt <= "01";
			 else
			   if (byte_cnt = "01") then
			     byte_cnt <= "10";
			   else
			     if (byte_cnt = "10") then
			       byte_cnt <= "00";
			     end if;
			   end if;
			 end if;
		  else
          next_bit := std_logic_vector(unsigned(bit_cnt) - 1);
		  end if;
		  bit_cnt <= next_bit;
      end if;
    else
	   bit_cnt <= (others => '1');
		byte_cnt <= "00";
    end if;
  end process;
	
  process (sel, bit_cnt) is
  begin
	 if (sel = '0') then
	   if (byte_cnt = "00") then
	     miso <= inputs(to_integer(unsigned(bit_cnt)));
		else
	     if (byte_cnt = "01") then
	       miso <= inputs(8 + to_integer(unsigned(bit_cnt)));
		  else
	       if (byte_cnt = "10") then
	         miso <= inputs(16 + to_integer(unsigned(bit_cnt)));
		    end if;
		  end if;
		end if;
	 else 
      miso <= 'Z';
	 end if;
  end process;

  output <= p_out;

end RTL;

