----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    11:53:44 06/03/2013 
-- Design Name: 
-- Module Name:    gz_LCD - RTL 
-- Project Name: 
-- Target Devices: 
-- Tool versions: 
-- Description:    Drives a SSD1289 LCD controller and a
--                 ADS7843/XPT2046 touch screen controller.
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

entity gz_LCD is
    Port ( outputs : out  STD_LOGIC_VECTOR (15 downto 0) := (others => '0');
	        cs : out STD_LOGIC := '1';
			  wr : out STD_LOGIC := '1';
			  ena : out STD_LOGIC := '1';
			  pi_reset: in std_logic;
			  pi_cd: in std_logic;
			  reset: out std_logic;
			  cd: out std_logic;
           mosi : in  STD_LOGIC;
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
           miso : out  STD_LOGIC);
end gz_LCD;

architecture RTL of gz_LCD is
signal bit_cnt: natural range 0 to 15 := 15;
begin
  cs <= sel;
  reset <= pi_reset;
  cd <= pi_cd;
  ena <= '1';
  process (sclk, sel) is
  begin
  if (sel = '0') then
    if (rising_edge(sclk)) then
	   outputs(bit_cnt) <= mosi;
	 end if;
	 if (falling_edge(sclk)) then
	   if (bit_cnt = 0) then
		  bit_cnt <= 15;
		else
		  bit_cnt <= bit_cnt - 1;
		end if;
		if (bit_cnt > 0 and bit_cnt < 8) then
		  wr <= '0';
		else
		  wr <= '1';
		end if;
	 end if;
  else
    bit_cnt <= 15;
	 wr <= '1';
  end if;
  end process;
end RTL;

--entity gz_LCD is
--    Port ( outputs : out  STD_LOGIC_VECTOR (20 downto 0);
--	        lcd_cs : out STD_LOGIC;
--			  d_in : out STD_LOGIC;
--			  d_out : in STD_LOGIC;
--			  d_clk : out STD_LOGIC;
--			  d_cs  : out STD_LOGIC;
--			  sel1 : in STD_LOGIC;
--			  pen_irq: in STD_LOGIC;
--			  pi_irq: out STD_LOGIC;
--           mosi : in  STD_LOGIC;
--           sclk : in  STD_LOGIC;
--           sel : in  STD_LOGIC;
--           miso : out  STD_LOGIC);
--end gz_LCD;
--architecture RTL of gz_LCD is
--signal bit_cnt: natural range 0 to 32 := 0;
--begin
--  d_in <= mosi;
--  d_clk <= sclk;
--  d_cs <= sel1;
--  pi_irq <= pen_irq;
--  process (sclk, sel) is
--  variable shift: std_logic_vector (20 downto 0) := (others => '0');
--  begin
--	 if (sel = '0') then
--	   if (rising_edge(sclk)) then
--		  shift(20 downto 1) := shift(19 downto 0);
--		  shift(0) := mosi;
--		end if;
--		if (falling_edge(sclk)) then
--		  if (bit_cnt > 15) then
--		    outputs <= shift;
--			 lcd_cs <= '0';
--		  else
--			 lcd_cs <= '1';
--		  end if;
--		  if (bit_cnt = 32) then
--		    bit_cnt <= 1;
--		  else
--         bit_cnt <= bit_cnt + 1;
--		  end if;
--		end if;
--    else
--		lcd_cs <= '1';
--		bit_cnt <= 0;
--    end if;
--  end process;

--  process (sel, bit_cnt, sel1, d_out) is
--  begin
--	 if (sel = '0') then
--	   miso <= '0';
--	 else
--      if (sel1 = '0') then
--        miso <= d_out;
--      else
--        miso <= 'Z';
--		end if;
--	 end if;
--  end process;

--end RTL;

--architecture RTL of gz_LCD is
--signal bit_cnt: natural range 0 to 23 := 23;
--begin
--  d_in <= mosi;
--  d_clk <= sclk;
--  d_cs <= sel1;
--  pi_irq <= pen_irq;
--  process (sclk, sel) is
--  begin
--	 if (sel = '0') then
--	   if (rising_edge(sclk)) then
--	     if (bit_cnt < 21) then
--		    outputs(bit_cnt) <= mosi;
--	     end if;
--		end if;
--		if (falling_edge(sclk)) then
--		  if (bit_cnt = 0) then
--		    bit_cnt <= 23;
--		  else
--        bit_cnt <= bit_cnt - 1;
--		  end if; 
--		  if (bit_cnt < 16) then
--			 lcd_cs <= '0';
--		  else
--			 lcd_cs <= '1';
--		  end if;
--		end if;
--    else
--		lcd_cs <= '1';
--		bit_cnt <= 23;
--    end if;
--  end process;
--
--  process (sel, bit_cnt, sel1, d_out) is
--  begin
--	 if (sel = '0') then
--	   miso <= '0';
--	 else
--      if (sel1 = '0') then
--        miso <= d_out;
--      else
--        miso <= 'Z';
--		end if;
--	 end if;
--  end process;
--
--end RTL;