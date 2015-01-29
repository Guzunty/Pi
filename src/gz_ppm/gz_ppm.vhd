----------------------------------------------------------------------------------
-- Company:        Guzunty 
-- Engineer:       CDM
-- 
-- Create Date:    15:23:47 01/20/2015 
-- Design Name:    gz_ppm
-- Module Name:    gz_ppm - Behavioral
-- Project Name:   gz_ppm
-- Target Devices: XC9572XL
-- Tool versions:  ISE 14
-- Description:    Decodes Pulse Position Modulation input to
--                 up to eight servo signal outputs, plus pulse
--                 width digitisation with an interrupt driven
--                 SPI interface.
-- Dependencies: 
-- 
-- Revision 1.5
-- Additional Comments: Requires Fitter setting - optimise for area.
--                      Radio tested but not flight tested. Different
--                      flight rigs may produce different noise
--                      environments. You are strongly advised to fully
--                      exercise this core in a static test rig before
--                      flying it. Note that the pulse demultiplex
--                      operation is dependent on a hardware clock from
--                      the Raspberry Pi, so if that reboots, control
--                      could be lost. You could always make your own
--                      clock circuit if this is a concern, frequency
--                      needs to be about 2MHz.
----------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.NUMERIC_STD.ALL;

entity gz_ppm is
  Port ( ppm_i     : in  STD_LOGIC;
         ppm_clk_i : in  STD_LOGIC;
         sync_o    : out STD_LOGIC;
         pwms_o    : out  STD_LOGIC_VECTOR (7 downto 0);
         ppm_irq_o : out  STD_LOGIC := '0';
         miso_o    : out  STD_LOGIC;
         sclk_i    : in  STD_LOGIC;
         sel_i     : in  STD_LOGIC);
end gz_ppm;

architecture Behavioral of gz_ppm is
  signal channel_counter : unsigned (3 downto 0) := (others => '0');
  signal output_register: std_logic_vector(15 downto 0);
  signal reset : std_logic := '0';
  signal sync : std_logic := '0';
  signal servo : std_logic;
  signal irq_assert : std_logic;
  signal irq_deassert : std_logic;
begin

  servo <= ppm_i and sync and not reset;
  pwms_o <= (0 => servo, others => '0') when channel_counter = 0 else
            (1 => servo, others => '0') when channel_counter = 1 else
            (2 => servo, others => '0') when channel_counter = 2 else
            (3 => servo, others => '0') when channel_counter = 3 else
            (4 => servo, others => '0') when channel_counter = 4 else
            (5 => servo, others => '0') when channel_counter = 5 else
            (6 => servo, others => '0') when channel_counter = 6 else
            (7 => servo, others => '0') when channel_counter = 7 else
            (others => '0');
  sync_o <= sync;

  process (ppm_clk_i)
  variable pulse_width : unsigned (14 downto 0);
  variable Q0, Q1, Q2 : std_logic;
  variable ppm : std_logic := '0';
  begin
    if rising_edge(ppm_clk_i) then
      Q2 := Q1;
      Q1 := Q0;
      Q0 := ppm_i;
      if Q0 = '0' and Q1 = '0' and Q2 = '1' then
        -- falling edge of ppm signal (debounced)
        ppm := '0';
      end if;
      if Q0 = '1' and Q1 = '1' and Q2 = '0' then
        -- rising edge of ppm signal (debounced)
        irq_deassert <= '1';
        pulse_width := (others => '0');
        ppm := '1';
      else
        irq_deassert <= '0';
      end if;
      if pulse_width = 32767 then
        sync <= '0';
        if ppm = '0' then
          pulse_width := (others => '0');
        end if;
      else
        if ppm = '1' then
          pulse_width := pulse_width + 1;
          if pulse_width > 4100 then
            reset <= '1';
            sync <= '1';
          else
            reset <= '0';
          end if;
        end if;
      end if;
      if Q0 = '0' and Q1 = '0' and Q2 = '1' then
        if reset = '1' then
          channel_counter <= (others => '0');
        else
		    output_register(15) <= '0';            -- register bit not used
          output_register(14 downto 12) <= std_logic_vector(channel_counter(2 downto 0));
          output_register(11 downto 0) <= std_logic_vector(pulse_width(11 downto 0));
          irq_assert <= '1';
			 -- If we are on the 8th pulse, we are hitting the FrSky case where
          -- the interframe pulse is indistinguishable from a servo pulse. By
			 -- resetting the counter, we will continue to output pulses and
			 -- produce accurate digitisations of the pulse widths so long as
			 -- there are no glitch pulses. As soon as one of the channels de-
			 -- saturates, synchronisation is restored within one frame in any
			 -- case.
          if channel_counter < 8 then
            channel_counter <= channel_counter + 1;
          else
            channel_counter <= (others => '0');
          end if;
        end if;
      else
        irq_assert <= '0';
      end if;
    end if;
  end process;

  process (sclk_i, sel_i, ppm_i) is
  variable bit_cnt: unsigned (3 downto 0) := (others => '1');
  begin
    if (sel_i = '0') then
      if bit_cnt = 15 then
        miso_o <= ppm_i;                         -- potentially useful to detect irq latency issues
      else
        miso_o <= output_register(to_integer(bit_cnt));
      end if;
      if (falling_edge(sclk_i)) then
        if (bit_cnt = 0) then
          bit_cnt := (others => '1');
        else
          bit_cnt := bit_cnt - 1;
        end if;
      end if;
    else
      bit_cnt := (others => '1');
      miso_o <= 'Z';
    end if;
  end process;

  process (irq_assert, sel_i, irq_deassert) is
  begin
    if rising_edge(irq_assert) then
      ppm_irq_o <= sync;
    end if;
    if (sel_i = '0' or irq_deassert = '1') then
      ppm_irq_o <= '0';
    end if;
  end process;

end Behavioral;
