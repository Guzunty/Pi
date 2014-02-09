----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    14:29:26 07/02/2013 
-- Design Name: 
-- Module Name:    gz_ppm - Behavioral 
-- Project Name: 
-- Target Devices: 
-- Tool versions: 
-- Description: 
--
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

entity gz_ppm is
    Port ( ppm_in : in  STD_LOGIC;
	        sync : out STD_LOGIC;
           ppm_int_req : out  STD_LOGIC;
           ppm_clk : in  STD_LOGIC;
           miso : out  STD_LOGIC;
           sclk : in  STD_LOGIC;
           sel : in  STD_LOGIC;
			  pwms : out STD_LOGIC_VECTOR (7 downto 0) := (others => '0')
	 );
end gz_ppm;

architecture Behavioral of gz_ppm is
signal pulse_width_counter: unsigned(14 downto 0) := (others => '0');
signal output_register: unsigned(15 downto 0) := (others => '1');
signal inter_frame: std_logic := '0';
signal interframe_overrun: std_logic := '0';
begin

-- output_register(12) is the synchronized flag
ppm_int_req <= (not inter_frame) and (not interframe_overrun)
                           and output_register(12) and (not ppm_in);
sync <= output_register(12);

process(ppm_clk, ppm_in) is
variable reset: std_logic := '0';
begin
  if (ppm_in = '0' and reset = '0') then
    pulse_width_counter <= (others => '0');
	 reset := '1';
  elsif (rising_edge(ppm_clk)) then
    if (ppm_in = '1') then
      reset := '0';
	 end if;
    if (pulse_width_counter = 32767) then
	   -- reset condition, or 16ms passed
	   -- without a ppm pulse, signal is lost.
	   output_register(12) <= '0'; -- signal synchronization lost
    else
	   if (pulse_width_counter > 4301) then
        -- Detected inter frame gap (count of 4301 ~= 2.1 ms)
	     output_register(12) <= '1'; -- signal synchronization achieved
		  inter_frame <= '1';
		else
		  if (pulse_width_counter > 1023) then -- allow pulse to end (0.3ms)
		    inter_frame <= '0';
		  end if;
		end if;
      pulse_width_counter <= pulse_width_counter + 1;
	 end if;
  end if; -- pulse clock rising edge
end process;

process(ppm_in) is
variable pulse_counter: unsigned(3 downto 0) := (others => '0');
begin
  if (falling_edge(ppm_in)) then
    if (inter_frame = '1') then
      pulse_counter := (others => '0');  -- reset channel counter
		if (output_register(12) = '1') then
		  pwms(0) <= '1';
		end if;
    else
      -- increment channel count
      if (pulse_counter /= 8) then
		  if (output_register(12) = '1') then
	       pwms(to_integer(pulse_counter)) <= '0';
		  end if;
		  interframe_overrun <= '0';
        output_register(15 downto 13) <= pulse_counter(2 downto 0);
        pulse_counter := pulse_counter + 1;
		  if (output_register(12) = '1' and pulse_counter /= 8) then
	       pwms(to_integer(pulse_counter)) <= '1';
		  end if;
      else
		  interframe_overrun <= '1';
        pulse_counter := (others => '0'); -- handle Fr Sky Receiver @ 18 ms frame time
      end if;
      -- load the current channel to output register
      output_register(11 downto 0) <= pulse_width_counter(11 downto 0); 
    end if;  -- in-frame pulse
  end if; -- ppm_in falling edge
end process;

process (sclk, sel) is
variable bit_cnt: natural range 0 to 15 := 15;
begin
  if (sel = '0') then
    if (rising_edge(sclk)) then
      miso <= output_register(bit_cnt);
    end if;
    if (falling_edge(sclk)) then
      if (bit_cnt = 0) then
        bit_cnt := 15;
      else
        bit_cnt := bit_cnt - 1;
      end if;
    end if;
  else
    bit_cnt := 15;
    miso <= 'Z';
  end if;
end process;

end Behavioral;
