--------------------------------------------------------------------------------
-- Company:         Guzunty
-- Engineer:        campbellsan
--
-- Create Date:     22:59:18 12/21/2012
-- Design Name:     gzleddrvr
-- Module Name:     gzleddrvr/gzleddrvr_testbench.vhd
-- Project Name:    Guzunty LED Driver
-- Target Device:   XC9572XL
-- Tool versions:   ISE 14.3
-- Description:     Testbench to test the LED Driver for the Guzunty PI SB
--          
--------------------------------------------------------------------------------
LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.numeric_std.ALL;

library work;
use work.all;

ENTITY gzleddrvr_testbench IS
    GENERIC (   
        N : positive := 8                                  -- 32bit serial word length is default
    );                                          
END gzleddrvr_testbench;
 
ARCHITECTURE behavior OF gzleddrvr_testbench IS 

    --=========================================================
    -- Component declaration for the Unit Under Test (UUT)
    --=========================================================

	COMPONENT gzleddrvr is
    Port ( mosi : in  STD_LOGIC;
           miso : out  STD_LOGIC;
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
           clk : in  STD_LOGIC;
			  digit_enas : out  std_logic_vector (3 downto 0);
			  segments: out std_logic_vector (6 downto 0);
			  inputs : in std_logic_vector (5 downto 0));
	END COMPONENT;
   for Inst_gzleddrvr: gzleddrvr use entity work.gzleddrvr;

    --=========================================================
    -- constants
    --=========================================================
    constant fifo_memory_size : integer := 16;
    constant digit_memory_size : integer := 4;
    
    --=========================================================
    -- types
    --=========================================================
    type fifo_memory_type is array (0 to fifo_memory_size-1) of std_logic_vector (N-1 downto 0);
    type digit_memory_type is array (0 to digit_memory_size-1) of std_logic_vector (N-1 downto 0);

    --=========================================================
    -- signals to connect the instances
    --=========================================================
    -- internal clk and rst
    signal m_clk : std_logic := '0';                -- clock domain for the master parallel interface. Must be faster than spi bus sck.
    signal s_clk : std_logic := '0';                -- clock domain for the slave parallel interface. Must be faster than spi bus sck.
    signal rst : std_logic := 'U';
    -- spi bus wires
    signal spi_sck : std_logic;
    signal spi_ssel : std_logic := '1';             -- start disabled
    signal spi_miso : std_logic;
    signal spi_mosi : std_logic;
	 signal m_inputs : std_logic_vector (7 downto 0);

    --=========================================================
    -- Clock period definitions
    --=========================================================
    constant s_clk_period : time := 10 ns;          -- 100MHz slave parallel clock

BEGIN
 
    --=========================================================
    -- Component instantiation for the Unit Under Test (UUT)
    --=========================================================

    Inst_gzleddrvr: gzleddrvr
	 port map ( mosi => spi_mosi,
           miso => spi_miso,
           sclk => spi_sck,
           sel => spi_ssel,
           clk => s_clk,
			  inputs => m_inputs (5 downto 0));

    --=========================================================
    -- Clock generator processes
    --=========================================================
    s_clk_process : process
    begin
        s_clk <= '0';
        wait for s_clk_period/2;
        s_clk <= '1';
        wait for s_clk_period/2;
    end process s_clk_process;

    spi_clk_process : process
	 begin
	   spi_sck <= '0';
		wait for s_clk_period*10;
		if (spi_ssel = '0') then
		  spi_sck <= '1';
		end if;
		wait for s_clk_period*10;
	 end process spi_clk_process;
	 
    --=========================================================
    -- rst_i process
    --=========================================================
    rst <= '0', '1' after 20 ns, '0' after 100 ns;
    
    --=========================================================
    -- Slave interface process
    --=========================================================
    slave_tx_fifo_proc: process is
        variable fifo_memory : fifo_memory_type := 
            (X"31",X"97",X"ef",X"ba",X"e5",X"51",X"84",X"91",
             X"c5",X"2f",X"ca",X"af",X"1a",X"f0",X"7e",X"12");
        variable fifo_head : integer range 0 to fifo_memory_size-1;
		  variable test_digits : digit_memory_type :=
		      (X"5b",X"3f",X"06",X"4f");
		  variable digit_head : integer range 0 to digit_memory_size-1;
    begin
        -- synchronous rst_i
        wait until rst = '1';
        wait until s_clk'event and s_clk = '1';
        m_inputs <= (others => '0');
        --wren_s <= '0';
        fifo_head := 0;
        wait until rst = '0';
		  spi_ssel <= '0';
		  digit_head := 3;
        --wait until di_req_s = '1';                          -- wait shift register request for data
        -- load next fifo contents into shift register
        for cnt in 0 to fifo_memory_size-1 loop
            fifo_head := cnt;                              -- pre-compute next pointer 
            for j in 0 to 7 loop
				  spi_mosi <= test_digits(digit_head)(7 - j);
				  wait until spi_sck'event and spi_sck = '0';  -- sync fifo data load at next rising edge
				end loop;
			   if (digit_head = 0) then
			     digit_head := 3;
			   else
			     digit_head := digit_head - 1;
			   end if;
            m_inputs <= fifo_memory(fifo_head);                 -- place data into tx_data input bus
            --wait until s_clk'event and s_clk = '1';         -- sync fifo data load at next rising edge
            --wren_s <= '1';                                  -- write data into shift register
            --wait until s_clk'event and s_clk = '1';         -- sync fifo data load at next rising edge
            --wait until s_clk'event and s_clk = '1';         -- sync fifo data load at next rising edge
            --wren_s <= '0';                                  -- remove write enable signal
            --wait until di_req_s = '1';                      -- wait shift register request for data
        end loop;
		  spi_ssel <= '1';
        wait;
    end process slave_tx_fifo_proc;
 
END ARCHITECTURE behavior;
