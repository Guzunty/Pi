--------------------------------------------------------------------------------
-- Company:       Guzunty
-- Engineer:      campbellsan
--
-- Create Date:   10:30:05 01/09/2013
-- Design Name:   Guzunty 24 in 1 direct out
-- Module Name:   gz_24i1O_testbench.vhd
-- Project Name:  gz_24i1O
-- Target Device: XC9572XL-PC44
-- Tool versions: ISE 14.3
-- Description:   Unit under test provides general IO, 24 ins and 1 direct out
-- 
-- VHDL Test Bench Created by ISE for module: gz_24i1O
-- 
-- Dependencies:  None
-- 
-- Revision:      1.0
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
 
ENTITY gz_24i1O_testbench IS
END gz_24i1O_testbench;
 
ARCHITECTURE behavior OF gz_24i1O_testbench IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT gz_24i1O
    PORT(
         inputs : IN  std_logic_vector(23 downto 0);
         output : OUT  std_logic;
         mosi : IN  std_logic;
         miso : OUT  std_logic;
         sclk : IN  std_logic;
         sel : IN  std_logic;
         p_out : IN  std_logic
        );
    END COMPONENT;
    

   --Inputs
   signal inputs : std_logic_vector(23 downto 0) := (others => '0');
   signal mosi : std_logic := '0';
   signal sclk : std_logic := '0';
   signal sel : std_logic := '1';
   signal p_out : std_logic := '0';

 	--Outputs
   signal output : std_logic;
   signal miso : std_logic;

   -- Clock period definitions
   constant sclk_period : time := 10 ns;
 
BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: gz_24i1O PORT MAP (
          inputs => inputs,
          output => output,
          mosi => mosi,
          miso => miso,
          sclk => sclk,
          sel => sel,
          p_out => p_out
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
		inputs <= "101010101010101010101010";
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
		p_out <= '1';
      wait for 100 ns;
		p_out <= '0';
		sel <= '0';
		mosi <= '0';
      wait for sclk_period*6;  -- value: 02H
		p_out <= '1';     
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
		p_out <= '0';
      wait for 100 ns;
		inputs <= "000011110000111100001111";
		p_out <= '0';
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
		p_out <= '1';
		mosi <= '0';            -- value: 04H
		wait for sclk_period*5;
		mosi <= '1';
      wait for sclk_period;
		mosi <= '0';
      wait for sclk_period*2;
		mosi <= '0';            -- value 01H
		wait for sclk_period*7;
		mosi <= '1';
		p_out <= '0';
      wait for sclk_period;
		mosi <= '0';
      sel <= '1';
      wait;
   end process;

END;
