----------------------------------------------------------------------------------
-- Company:        Guzunty
-- Engineer:       campbellsan
-- 
-- Create Date:    09:39:50 01/12/2013 
-- Design Name:    GuzuntyPiSB Test core
-- Module Name:    gz_test - RTL 
-- Project Name:   gz_test
-- Target Devices: XC9572XL-PC44
-- Tool versions:  ISE 14.3
-- Description:    Tests the guzunty pi io expander 
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
use IEEE.std_logic_unsigned.all;

entity gz_test is
    Port ( clk : in STD_LOGIC;
	        test_port_a : out  STD_LOGIC_VECTOR (7 downto 0);
           test_port_b : out  STD_LOGIC_VECTOR (7 downto 0);
           test_port_c : out  STD_LOGIC_VECTOR (7 downto 0);
           test_port_d : out  STD_LOGIC_VECTOR (7 downto 0);
           test_pin : out  STD_LOGIC);
end gz_test;

architecture RTL of gz_test is
signal counter : STD_LOGIC_VECTOR (20 downto 0);
begin
  test: process (clk) is
  begin
    if (rising_edge(clk)) then
      counter <= counter + '1';
      test_port_a <= counter (20 downto 13); -- Write upper 8 bits
      test_port_b <= counter (20 downto 13); -- of counter to the
      test_port_c <= counter (20 downto 13); -- four ports so that
      test_port_d <= counter (20 downto 13); -- all pins are tested
	   test_pin <= counter (20);              -- test single bit left over
    end if;
  end process test;
end RTL;

