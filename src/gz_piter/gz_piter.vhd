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
--                 - two 6 bit Servo controllers with SPI interface.
--                 - two 8 bit 'true color' PWM sets
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
			  pwms : out STD_LOGIC_VECTOR (5 downto 0);
           servo_sel : in  STD_LOGIC;
           servo_clk : in  STD_LOGIC);
			  
end gz_piter;

architecture RTL of gz_piter is
signal bit_cnt: std_logic_vector(3 downto 0);
type servo_counter_t is array (1 downto 0) of std_logic_vector(5 downto 0);
signal servo_counters: servo_counter_t := (("100000"), ("100000"));
type pwm_counter_t is array (1 downto 0) of std_logic_vector(7 downto 0);
signal pwm_counters: pwm_counter_t := ((others => '1'), (others => '1'));
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
	 variable reg_selector: std_logic_vector(1 downto 0);
	 variable bit_num: integer range 0 to 7;
  begin
	 if (servo_sel = '0') then
	   if (rising_edge(p_sclk)) then
	     if (bit_cnt(3) = '1') then -- loading reg selector
		    if (bit_cnt(0) = '1') then
            reg_selector(1) := p_mosi;
			 else
			   reg_selector(0) := p_mosi; 
			 end if;
   	  else                      -- loading value;
		    bit_num := to_integer(unsigned(bit_cnt(2 downto 0)));
			 if(reg_selector(1) = '0') then  -- servo value
			   if(bit_num < 6) then
			     if (reg_selector(0) = '0') then
	             servo_counters(0)(bit_num) <= p_mosi;
				  else
				    servo_counters(1)(bit_num) <= p_mosi;
				  end if;
				end if;
			 else                            -- pwm value
			   if (reg_selector(0) = '0') then
				  pwm_counters(0)(bit_num) <= p_mosi;
				else
				  pwm_counters(1)(bit_num) <= p_mosi;
				end if;
			 end if;
		  end if;
	   end if;
		if (falling_edge(p_sclk)) then
        bit_cnt <= std_logic_vector(unsigned(bit_cnt) - 1);
      end if;
		p_miso <= '0';                 -- We currently have nothing to write to SPI
    else
		reg_selector := "00";
	   bit_cnt <= "1111";
		p_miso <= 'Z';
    end if;
  end process;

  process (servo_clk, servo_counters) is
	 variable main_counter: std_logic_vector(10 downto 0):= (others => '0');
  begin
	 if (rising_edge(servo_clk)) then
	   main_counter := std_logic_vector(unsigned(main_counter) + 1);
		if (main_counter = "00000000000") then
		  servos <= (others => '1');
		end if;
	   if (main_counter(6 downto 1) = "000000") then
		  pwms <= (others => '1');
	   end if;
	 end if;
    for I in 0 to 1 loop
      if (main_counter = "00010" & servo_counters(I)) then
	     servos(I) <= '0';
	   end if;
		-- Colour registers have the format RRRGGGBB
		if (main_counter(3 downto 1) = pwm_counters(I)(7 downto 5)) then
		  pwms(I) <= '0';     -- Red
		end if;
		if (main_counter(3 downto 1) = pwm_counters(I)(4 downto 2)) then
		  pwms(I + 2) <= '0'; -- Green
		end if;
		if (main_counter(3 downto 2) = pwm_counters(I)(1 downto 0)) then
		  pwms(I + 4) <= '0'; -- Blue
		end if;
	 end loop;
  end process;

end RTL;