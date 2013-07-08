--------------------------------------------------------------------------------
-- Company: 
-- Engineer:
--
-- Create Date:   16:16:55 06/20/2013
-- Design Name:   
-- Module Name:   /media/sf_Projects/Raspberry Pi/Guzunty/SB/gz_LCD/gz_LCD_testbench.vhd
-- Project Name:  gz_LCD
-- Target Device:  
-- Tool versions:  
-- Description:   
-- 
-- VHDL Test Bench Created by ISE for module: gz_LCD
-- 
-- Dependencies:
-- 
-- Revision:
-- Revision 0.01 - File Created
-- Additional Comments:
--
-- Notes: 
-- This testbench has been automatically generated using types std_logic and
-- std_logic_vector for the ports of the unit under test.  Xilinx recommends
-- that these types always be used for the top-level I/O of a design in order
-- to guarantee that the testbench will bind correctly to the post-implementation 
-- simulation model.
--------------------------------------------------------------------------------
LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
 
-- Uncomment the following library declaration if using
-- arithmetic functions with Signed or Unsigned values
--USE ieee.numeric_std.ALL;
 
ENTITY gz_LCD_testbench IS
END gz_LCD_testbench;
 
ARCHITECTURE behavior OF gz_LCD_testbench IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT gz_LCD
    PORT(
         outputs : OUT  std_logic_vector(15 downto 0);
         cs : OUT  std_logic;
         wr : OUT  std_logic;
         mosi : IN  std_logic;
         sclk : IN  std_logic;
         sel : IN  std_logic;
         miso : OUT  std_logic
        );
    END COMPONENT;
    

   --Inputs
   signal mosi : std_logic := '0';
   signal sclk : std_logic := '0';
   signal sel : std_logic := '0';

   signal gated_clk : std_logic := '0';
 	--Outputs
   signal outputs : std_logic_vector(15 downto 0);
   signal cs : std_logic;
   signal wr : std_logic;
   signal miso : std_logic;

   -- Clock period definitions
   constant sclk_period : time := 10 ns;
 
BEGIN
  gated_clk <= (sclk and (not sel));
	-- Instantiate the Unit Under Test (UUT)
   uut: gz_LCD PORT MAP (
          outputs => outputs,
          cs => cs,
          wr => wr,
          mosi => mosi,
          sclk => gated_clk,
          sel => sel,
          miso => miso
        );

   -- Clock process definitions
   sclk_process :process
   begin
		sclk <= '0';
		wait for sclk_period/2;
		sclk <= '1';
		wait for sclk_period/2;
   end process;
 

   -- Stimulus process
   stim_proc: process
	variable op : std_logic := '0';
	variable count : natural range 0 to 72 :=0;
   begin		
      -- hold reset state for 100 ns.
      sel <= '1';
      wait for 100 ns;	
		mosi <= '0';
      wait for sclk_period*8;  -- value: 00H
      sel <= '0';
      wait for sclk_period*8;  -- value: 00H
      wait for sclk_period*8;  -- value: 00H
		sel <= '1';
	   wait for sclk_period / 2;
		sel <= '0';
		while (count < 16) loop
		  mosi <= op;
		  op := not op;
		  wait for sclk_period;
		  count := count + 1;
		end loop;
		mosi <= '0';
      sel <= '1';
	   wait for sclk_period / 2;
		op := '1';
		count := 0;
		sel <= '0';
		while (count < 16) loop
		  mosi <= op;
		  op := not op;
		  wait for sclk_period;
		  count := count + 1;
		end loop;
		mosi <= '0';
      sel <= '1';
	   wait for sclk_period / 2;
		op := '1';
		count := 0;
		sel <= '0';
		while (count < 64) loop
		  mosi <= op;
		  op := not op;
		  wait for sclk_period;
		  count := count + 1;
		  if (count = 24 or count = 48) then
		    op := not op; -- switch the test pattern
		  end if;
		end loop;
		mosi <= '0';
      sel <= '1';
	   wait for sclk_period / 2;
		sel <= '0';
		mosi <= '1';
		wait for sclk_period * 4;
		mosi <= '0';
		wait for sclk_period * 4;
		wait for sclk_period * 4;
		mosi <= '1';
		wait for sclk_period * 4;
		sel <= '1';
      wait;

   end process;

END;
