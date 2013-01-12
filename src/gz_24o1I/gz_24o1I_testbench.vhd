--------------------------------------------------------------------------------
-- Company: 
-- Engineer:
--
-- Create Date:   14:14:55 01/12/2013
-- Design Name:   
-- Module Name:   /media/sf_Projects/Raspberry Pi/Guzunty/SB/gz_24o1I/gz_24o1I_testbench.vhd
-- Project Name:  gz_24o1I
-- Target Device:  
-- Tool versions:  
-- Description:   
-- 
-- VHDL Test Bench Created by ISE for module: gz_24o1I
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
 
ENTITY gz_24o1I_testbench IS
END gz_24o1I_testbench;
 
ARCHITECTURE behavior OF gz_24o1I_testbench IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT gz_24o1I
    PORT(
         outputs : OUT  std_logic_vector(23 downto 0);
         input : IN  std_logic;
         mosi : IN  std_logic;
         sclk : IN  std_logic;
         sel : IN  std_logic;
         miso : OUT  std_logic;
         p_input : OUT  std_logic
        );
    END COMPONENT;
    

   --Inputs
   signal input : std_logic := '0';
   signal mosi : std_logic := '0';
   signal sclk : std_logic := '0';
   signal sel : std_logic := '1';

 	--Outputs
   signal outputs : std_logic_vector(23 downto 0);
   signal miso : std_logic;
   signal p_input : std_logic;

   -- Clock period definitions
   constant sclk_period : time := 10 ns;
 
BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: gz_24o1I PORT MAP (
          outputs => outputs,
          input => input,
          mosi => mosi,
          sclk => sclk,
          sel => sel,
          miso => miso,
          p_input => p_input
        );

   -- Clock process definitions
   sclk_process :process
   begin
		sclk <= '0';
		wait for sclk_period/2;
		if (sel = '0') then
		  sclk <= '1';
		end if;
		wait for sclk_period/2;
   end process;
 

   -- Stimulus process
   stim_proc: process
   begin		
      -- hold reset state for 100 ns.
      wait for 100 ns;
		input <= '1';
		mosi <= '0';
      sel <= '0';
      wait for sclk_period*8;  -- value: 00H
      wait for sclk_period*8;  -- value: 00H
		sel <= '1';
      wait for 100 ns;
		sel <= '0';
		mosi <= '0';
      wait for sclk_period*7;  -- value: 01H
		mosi <= '1';
      wait for sclk_period;
		mosi <= '0';             -- value: 08H
      wait for sclk_period*4;
		mosi <= '1';
		wait for sclk_period;
		mosi <= '0';
      wait for sclk_period*3;
		sel <= '1';
		input <= '1';
      wait for 100 ns;
		input <= '0';
		sel <= '0';
		mosi <= '0';
      wait for sclk_period*6;  -- value: 02H
		input <= '1';     
      mosi <= '1';
		wait for sclk_period;
		mosi <= '0';
		wait for sclk_period;
		mosi <= '0';            -- value: 10H
      wait for sclk_period*3;
		mosi <= '1';
		wait for sclk_period;
      mosi <= '0';
      wait for sclk_period*4;
		sel <= '1';
		input <= '0';
      wait for 100 ns;
		input <= '1';
		sel <= '0';
		mosi <= '0';
		wait for sclk_period*6; -- value: 03H
		mosi <= '1';
      wait for sclk_period;
      wait for sclk_period;
		mosi <= '0';            -- value: 18H
		wait for sclk_period*3;
		mosi <= '1';           
      wait for sclk_period*2;
		mosi <= '0';
      wait for sclk_period*3;
		mosi <= '0';            -- value: 07
		wait for sclk_period*5;
		mosi <= '1';
      wait for sclk_period*3;
		mosi <= '0';            -- value 1CH
		wait for sclk_period*3;
		mosi <= '1';           
      wait for sclk_period*3;
		mosi <= '0';
      wait for sclk_period*2;
		mosi <= '0';            -- value: 06H
		wait for sclk_period*5;
		mosi <= '1';
      wait for sclk_period*2;
		mosi <= '0';
      wait for sclk_period;
		mosi <= '0';            -- value 1FH
		wait for sclk_period*3;
		mosi <= '1';           
      wait for sclk_period*5;
		mosi <= '0';
		input <= '0';
		mosi <= '0';            -- value: 04H
		wait for sclk_period*5;
		mosi <= '1';
      wait for sclk_period;
		mosi <= '0';
      wait for sclk_period*2;
		mosi <= '0';            -- value 01H
		wait for sclk_period*7;
		mosi <= '1';
		input <= '1';
      wait for sclk_period;
		mosi <= '0';
      sel <= '1';
      wait;
   end process;

END;
