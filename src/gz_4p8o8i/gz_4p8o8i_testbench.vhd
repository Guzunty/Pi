--------------------------------------------------------------------------------
-- Company: 
-- Engineer:
--
-- Create Date:   12:26:05 01/14/2013
-- Design Name:   
-- Module Name:   /media/sf_Projects/Raspberry Pi/Guzunty/SB/gz_4p8o8i/gz_4p8o8i_testbench.vhd
-- Project Name:  gz_4p8o8i
-- Target Device:  
-- Tool versions:  
-- Description:   
-- 
-- VHDL Test Bench Created by ISE for module: gz_4p8o8i
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
 
ENTITY gz_4p8o8i_testbench IS
END gz_4p8o8i_testbench;
 
ARCHITECTURE behavior OF gz_4p8o8i_testbench IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT gz_4p8o8i
    PORT(
         pwms : OUT  std_logic_vector(3 downto 0);
         outputs : OUT  std_logic_vector(7 downto 0);
         inputs : IN  std_logic_vector(7 downto 0);
         mosi : IN  std_logic;
         sclk : IN  std_logic;
         sel : IN  std_logic;
         miso : OUT  std_logic;
         clk : IN  std_logic
        );
    END COMPONENT;
    

   --Inputs
   signal inputs : std_logic_vector(7 downto 0) := (others => '0');
   signal mosi : std_logic := '0';
   signal sclk : std_logic := '0';
   signal sel : std_logic := '1';
   signal clk : std_logic := '0';

 	--Outputs
   signal pwms : std_logic_vector(3 downto 0);
   signal outputs : std_logic_vector(7 downto 0);
   signal miso : std_logic;

   -- Clock period definitions
   constant sclk_period : time := 100 ns;
   constant clk_period : time := 10 ns;
 
BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: gz_4p8o8i PORT MAP (
          pwms => pwms,
          outputs => outputs,
          inputs => inputs,
          mosi => mosi,
          sclk => sclk,
          sel => sel,
          miso => miso,
          clk => clk
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
 
   clk_process :process
   begin
		clk <= '0';
		wait for clk_period/2;
		clk <= '1';
		wait for clk_period/2;
   end process;
 

   -- Stimulus process
   stim_proc: process
   begin		
      -- hold reset state for 100 ns.
      wait for 100 ns;
		inputs <= "10101010";
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
      wait for 100 ns;
		sel <= '0';
		mosi <= '0';
      wait for sclk_period*6;  -- value: 02H
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
      wait for 100 ns;
		inputs <= "00001111";
		sel <= '0';
		mosi <= '0';
		wait for sclk_period*6; -- value: 03H
		mosi <= '1';
      wait for sclk_period;
      wait for sclk_period;
		mosi <= '0';            -- value: 18H
		wait for sclk_period*4;
		mosi <= '1';           
      wait for sclk_period;
		mosi <= '0';
      wait for sclk_period*3;
		mosi <= '0';            -- value: 04H
		wait for sclk_period*5;
		mosi <= '1';
      wait for sclk_period*3;
		mosi <= '0';            -- value 1CH
		wait for sclk_period*3;
		mosi <= '1';           
      wait for sclk_period*3;
		mosi <= '0';
      wait for sclk_period*2;
		wait for sclk_period*6; -- value: 03H
		mosi <= '1';
      wait for sclk_period;
      wait for sclk_period;
		mosi <= '0';            -- value 1FH
		wait for sclk_period*3;
		mosi <= '1';           
      wait for sclk_period*5;
		mosi <= '0';
		mosi <= '0';            -- value: 04H
		wait for sclk_period*5;
		mosi <= '1';
      wait for sclk_period;
		mosi <= '0';
      wait for sclk_period*2;
		mosi <= '0';            -- value 01H
		wait for sclk_period*7;
		mosi <= '1';
      wait for sclk_period;
		mosi <= '0';
      wait for sclk_period*8;  -- value: 00H
		mosi <= '1';
      wait for sclk_period;    -- value: 80H
		mosi <= '0';
      wait for sclk_period*7;
		mosi <= '0';            -- value: 04H
		wait for sclk_period*5;
		mosi <= '1';
      wait for sclk_period;
		mosi <= '0';
      wait for sclk_period*2;
		mosi <= '1';            -- value: F0H
		wait for sclk_period*4;
		mosi <= '0';
		wait for sclk_period*4;
      sel <= '1';
      wait;
   end process;

END;
