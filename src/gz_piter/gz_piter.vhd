----------------------------------------------------------------------------------
-- Company:        Guzunty
-- Engineer:       campbellsan
-- 
-- Create Date:    21:45:54 12/31/2012 
-- Design Name:    gz_piter
-- Module Name:    gz_piter - RTL 
-- Project Name:   Guzunty PI SB Pi Terrestrial Robot Glue Chip
-- Target Devices: XC9500XL - PC44
-- Tool versions:  ISE 14.3
-- Description:    Provides:
--                 - a programming interface for an Arduino ICSP socket.
--                 - voltage isolation for two serial ports.
--                 - two 7 bit Servo controllers with SPI interface.
-- Dependencies:   None.
--
-- Revision: 0.01
-- Revision 0.01 - File Created
-- Additional Comments: 
--
----------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.NUMERIC_STD.ALL;

entity gz_piter is
    Port ( p_rx : out STD_LOGIC;
			  p_tx : in STD_LOGIC;
			  rx : out STD_LOGIC;
			  tx : in STD_LOGIC;
			  p_sclk : in  STD_LOGIC;
           sclk : out  STD_LOGIC;
           p_mosi : in  STD_LOGIC;
           mosi : out  STD_LOGIC;
           p_miso : out  STD_LOGIC;
           miso : in  STD_LOGIC;
           p_sel : in  STD_LOGIC;
           rst : out  STD_LOGIC;
			  p_srx : out STD_LOGIC;
			  p_stx : in STD_LOGIC;
			  gpsrx : out STD_LOGIC;
			  gpstx : in STD_LOGIC;
			  servos : out  STD_LOGIC_VECTOR (1 downto 0);
           servo_sel : in  STD_LOGIC;
           servo_clk : in  STD_LOGIC);
end gz_piter;

architecture RTL of gz_piter is
signal bit_cnt: std_logic_vector(2 downto 0);
type pwm_counter_t is array (1 downto 0) of std_logic_vector(6 downto 0);
signal pwm_counters: pwm_counter_t := (others=> (others=> '0'));
begin

  -- concurrent assignments  
  rst   <= p_sel;
  rx    <= p_tx ;
  p_rx  <= tx ;
  p_srx <= gpstx ;     -- GPS Transmit to Pi soft serial RX
  gpsrx <= p_stx ;     -- GPS Receive to Pi soft serial TX

  process (p_sel, p_sclk, p_mosi, miso) is
  begin
    if (p_sel = '0') then
      sclk  <= p_sclk;
      mosi  <= p_mosi;
      p_miso <= miso;
	 else
      sclk  <= 'Z';
      mosi  <= 'Z';
      p_miso <= 'Z';
		mosi <= 'Z';
	 end if;
  end process;

  process (servo_sel, p_sclk) is
	 variable pwm_selector: std_logic;
  begin
	 if (servo_sel = '0') then
	   if (rising_edge(p_sclk)) then
	     if (bit_cnt = "111") then -- loading servo selector
          pwm_selector := p_mosi;
   	  else                      -- loading servo value;
			 if (pwm_selector = '0') then
	         pwm_counters(0)(to_integer(unsigned(bit_cnt))) <= p_mosi;
			 else
			   pwm_counters(1)(to_integer(unsigned(bit_cnt))) <= p_mosi;
			 end if;
		  end if;
	   end if;
		if (falling_edge(p_sclk)) then
        bit_cnt <= std_logic_vector(unsigned(bit_cnt) - 1);
      end if;
		p_miso <= '0';                 -- We currently have nothing to write to SPI
    else
		pwm_selector := '0';
	   bit_cnt <= "111";
		p_miso <= 'Z';
    end if;
  end process;

  process (servo_clk, pwm_counters) is
	 variable main_counter: std_logic_vector(9 downto 0):= (others => '0');
  begin
	 if (rising_edge(servo_clk)) then
	   main_counter := std_logic_vector(unsigned(main_counter) + 1);
	   if (main_counter = "0000000000") then
		  servos <= (others => '1');
	   end if;
	 end if;
    for I in 0 to 1 loop
      if (main_counter = "001" & pwm_counters(I)) then
	     servos(I) <= '0';
	   end if;
	 end loop;
  end process;

end RTL;