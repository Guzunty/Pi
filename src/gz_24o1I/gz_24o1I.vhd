----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    13:29:44 01/12/2013 
-- Design Name: 
-- Module Name:    gz_24o1I - Behavioral 
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

-- Uncomment the following library declaration if using
-- arithmetic functions with Signed or Unsigned values
--use IEEE.NUMERIC_STD.ALL;

-- Uncomment the following library declaration if instantiating
-- any Xilinx primitives in this code.
--library UNISIM;
--use UNISIM.VComponents.all;

entity gz_24o1I is
    Port ( outputs : out  STD_LOGIC_VECTOR (23 downto 0);
           input : in  STD_LOGIC;
           mosi : in  STD_LOGIC;
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
           miso : out  STD_LOGIC;
			  p_input : out  STD_LOGIC);
end gz_24o1I;

architecture Behavioral of gz_24o1I is
signal bit_cnt: natural range 0 to 7 := 7;
begin
  process (sclk, sel) is
    variable next_bit: natural range 0 to 7 := 7;
	 variable byte_cnt: natural range 0 to 3 := 0;
  begin
	 if (sel = '0') then
	   if (rising_edge(sclk)) then
	     if (byte_cnt = 0) then
          outputs(bit_cnt) <= mosi;
   	  else
		    if (byte_cnt = 1) then
            outputs(8 + bit_cnt) <= mosi;
			 else
			   if (byte_cnt = 2) then
              outputs(16 + bit_cnt) <= mosi;
			   end if;
			 end if;
		  end if;
	   end if;
		if (falling_edge(sclk)) then
	     if (bit_cnt = 0) then
		    next_bit := 7;
			 if (byte_cnt = 2) then
			   byte_cnt := 0;
			 else
			   byte_cnt := byte_cnt + 1;
			 end if;
		  else
          next_bit := next_bit - 1;
		  end if;
		  bit_cnt <= next_bit;
      end if;
    else
	   bit_cnt <= 7;
		byte_cnt := 0;
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

  p_input <= input;

end Behavioral;

