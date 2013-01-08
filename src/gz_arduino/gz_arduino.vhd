----------------------------------------------------------------------------------
-- Company:        Guzunty
-- Engineer:       campbellsan
-- 
-- Create Date:    21:45:54 12/31/2012 
-- Design Name:    gz_arduino
-- Module Name:    gz_arduino - RTL 
-- Project Name:   Guzunty PI SB Aurduino interface
-- Target Devices: XC9500XL - PC44
-- Tool versions:  ISE 14.3
-- Description:    Provides a programming interface for an
--                 Arduino ICSP socket.
-- Dependencies:   None.
--
-- Revision: 0.01
-- Revision 0.01 - File Created
-- Additional Comments: 
--
----------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity gz_arduino is
    Port ( p_sclk : in  STD_LOGIC;
           sclk : out  STD_LOGIC;
           p_mosi : in  STD_LOGIC;
           mosi : out  STD_LOGIC;
           p_miso : out  STD_LOGIC;
           miso : in  STD_LOGIC;
           p_sel : in  STD_LOGIC;
           rst : out  STD_LOGIC);
end gz_arduino;

architecture RTL of gz_arduino is

begin
  process (p_sel) is
  begin
    if (p_sel = '0') then
      sclk  <= p_sclk;
      mosi  <= p_mosi;
      p_miso <= miso;
	 else
      sclk  <= 'Z';
      mosi  <= 'Z';
      p_miso <= 'Z';
	 end if;
  end process;
  
  rst   <= p_sel;

end RTL;

