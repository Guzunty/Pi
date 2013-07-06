--------------------------------------------------------------------------------
-- Company: 
-- Engineer:
--
-- Create Date:   13:48:11 07/05/2013
-- Design Name:   
-- Module Name:   /media/sf_Projects/Raspberry Pi/Guzunty/SB/gz_ppm/gz_ppm_testbench.vhd
-- Project Name:  gz_ppm
-- Target Device:  
-- Tool versions:  
-- Description:   
-- 
-- VHDL Test Bench Created by ISE for module: gz_ppm
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
 
ENTITY gz_ppm_testbench IS
END gz_ppm_testbench;

ARCHITECTURE behavior OF gz_ppm_testbench IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT gz_ppm
    PORT(
         ppm_in : IN  std_logic;
			sync : OUT std_logic;
         ppm_int_req : OUT  std_logic;
         ppm_clk : IN  std_logic;
         miso : OUT  std_logic;
         sclk : IN  std_logic;
         sel : IN  std_logic;
		   pwms : out STD_LOGIC_VECTOR (7 downto 0)
        );
    END COMPONENT;
    

   --Inputs
   signal ppm_in : std_logic := '1';
   signal ppm_clk : std_logic := '0';
   signal sclk : std_logic := '0';
   signal sel : std_logic := '1';

 	--Outputs
   signal ppm_int_req : std_logic;
   signal miso : std_logic;
	signal sync : std_logic;
	signal pwms : std_logic_vector(7 downto 0);

   -- Clock period definitions
   constant ppm_clk_period : time := 480 ns; -- ~ 1024 x 10^3 Hz
   constant sclk_period : time := 62.5 ns;   -- 16 MHz
 
   procedure normal_frame(signal ppm_in: out std_logic; max: time; min: time) is
	variable center: time := ((max - min) / 2) + min ;
	constant w: time := 0.3 ms; -- ppm pulse width
	begin
		ppm_in <= '0';   -- frame start
		wait for w;
		ppm_in <= '1';
		wait for max - w; -- maximum

		ppm_in <= '0';   -- channel 1 end
		wait for w;
		ppm_in <= '1';
		wait for min - w; -- minimum
		
		ppm_in <= '0';   -- channel 2 end
		wait for w;
		ppm_in <= '1';
		wait for center - w;   -- center

		ppm_in <= '0';   -- channel 3 end
		wait for w;
		ppm_in <= '1';
		wait for min - w; -- minimum
		
		ppm_in <= '0';   -- channel 4 end
		wait for w;
		ppm_in <= '1';
		wait for max - w; -- maximum

		ppm_in <= '0';   -- channel 5 end
		wait for w;
		ppm_in <= '1';
		wait for min - w; -- minimum
		
		ppm_in <= '0';   -- channel 6 end
		wait for w;
		ppm_in <= '1';
		wait for max - w; -- maximum

		ppm_in <= '0';   -- channel 7 end
		wait for w;
		ppm_in <= '1';
		wait for center - w;   -- center

		ppm_in <= '0';   -- channel 8 end
		wait for w;
		ppm_in <= '1';
	end normal_frame;

BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: gz_ppm PORT MAP (
          ppm_in => ppm_in,
			 sync => sync,
          ppm_int_req => ppm_int_req,
          ppm_clk => ppm_clk,
          miso => miso,
          sclk => sclk,
          sel => sel,
			 pwms => pwms
        );

   -- Clock process definitions
   ppm_clk_process :process
   begin
		ppm_clk <= '0';
		wait for ppm_clk_period/2;
		ppm_clk <= '1';
		wait for ppm_clk_period/2;
   end process;
 
   sclk_process :process
   begin
		sclk <= '0';
		wait for sclk_period/2;
		sclk <= '1';
		wait for sclk_period/2;
   end process; 

   -- Stimulus process
   stim_proc: process
   begin		
      -- hold reset state for 100 ns.
      wait for 100 ns;

      wait for ppm_clk_period*10;
		wait for 4 ms;
      normal_frame(ppm_in, 2 ms, 1 ms);     -- test mixed case
		wait for 8 ms;   -- inter-frame
      normal_frame(ppm_in, 1.8 ms, 1.2 ms); -- following frame test
		wait for 8 ms;
		normal_frame(ppm_in, 2 ms, 2 ms);     -- all maximum frame
		wait for 4 ms;
		normal_frame(ppm_in, 2 ms, 2 ms);     -- another all maximum
		wait for 4 ms;
		normal_frame(ppm_in, 1 ms, 1 ms);     -- all minimum frame
		wait for 8 ms;
		normal_frame(ppm_in, 1 ms, 1 ms);     -- another all minimum
      wait for 32 ms;
		normal_frame(ppm_in, 2 ms, 1 ms);     -- long wait
      wait for 8 ms;
		normal_frame(ppm_in, 2 ms, 2 ms);     -- all maximum frame
		wait for 2 ms;                        -- Fr Sky case
		normal_frame(ppm_in, 2 ms, 2 ms);     -- all maximum frame
		wait for 2 ms;                        -- Fr Sky case
		normal_frame(ppm_in, 2 ms, 2 ms);     -- all maximum frame
		wait for 2 ms;                        -- 3rd Fr Sky case
		normal_frame(ppm_in, 2 ms, 2 ms);     -- all maximum frame
		wait for 8 ms;
      wait;
   end process;
	
	spi_proc: process (ppm_int_req, sclk) is
	variable bit_count: natural range 0 to 15 := 0;
	begin
	  if (rising_edge(ppm_int_req)) then
		 sel <= '0' after 50 us; -- simulate interrupt latency
	  end if;
	  if (rising_edge(sclk) and sel = '0') then
	    if (bit_count /= 15) then
		   bit_count := bit_count + 1;
		 else
		   bit_count := 0;
		   sel <= '1';-- SPI transfer complete
		 end if;
	  end if;
	end process;
END;
