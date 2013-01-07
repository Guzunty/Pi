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
signal bit_cnt: std_logic_vector (2 downto 0) := (others => '1');
begin
  process (sclk, sel) is
    variable next_bit: std_logic_vector(2 downto 0);
	 variable byte_cnt: std_logic_vector(1 downto 0) := "00";
  begin
	 if (sel = '0') then
	   if (rising_edge(sclk)) then
	     if (byte_cnt = "00") then
          outputs(to_integer(unsigned(bit_cnt))) <= mosi;
   	  else -- loading upper byte of set
	       if (byte_cnt = "01") then
            outputs(8 + to_integer(unsigned(bit_cnt))) <= mosi;
   	    else -- loading upper byte of set
	         if (byte_cnt = "10") then
              outputs(16 + to_integer(unsigned(bit_cnt))) <= mosi;
   	      else -- loading upper byte of set
	           if (byte_cnt = "11") then
				    if (bit_cnt = "000") then
                  outputs(24) <= mosi;
					 end if;
		        end if;
		      end if;
		    end if;
		  end if;
	   end if;
		if (falling_edge(sclk)) then
	     if (bit_cnt = "000") then
		    next_bit := (others => '1');
			 -- increment byte_cnt
			 if (byte_cnt = "00") then
			   byte_cnt := "01";
			 else
			   if (byte_cnt = "01") then
			     byte_cnt := "10";
			   else
			     if (byte_cnt = "10") then
			       byte_cnt := "11";
			     else -- must be "11"
			       byte_cnt := "00";
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
		byte_cnt := "00";
    end if;
  end process;

  process (sel, bit_cnt) is
  begin
	 if (sel = '0') then
	   miso <= '0';
	 else 
      miso <= 'Z';
	 end if;
  end process;

end RTL;

