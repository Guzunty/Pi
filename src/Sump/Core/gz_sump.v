`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    21:45:23 01/09/2013 
// Design Name: 
// Module Name:    gz_sump 
// Project Name: 
// Target Devices: 
// Tool versions: 
// Description: 
//
// Dependencies: 
//
// Revision: 
// Revision 0.01 - File Created
// Additional Comments: 
//
//////////////////////////////////////////////////////////////////////////////////
/*
Macrocells Used Pterms Used Registers Used Pins Used Function Block Inputs Used
68/72  (95%) 229/360  (64%) 65/72  (91%) 34/34  (100%) 147/216  (69%)
 
 
*/
 
/*
isim force add {/sram_counter/osc_clock} 1 -radix bin -value 0 -radix bin -time 500 ps -repeat 1 ns
isim force add {/sram_counter/pic_clock} 1 -radix bin -value 0 -radix bin -time 500 ps -repeat 2 ns
isim force add {/sram_counter/ext_clock} 1 -radix bin -value 0 -radix bin -time 500 ps -repeat 3 ns
isim force add {/sram_counter/mode_setup} 0 -radix bin
isim force add {/sram_counter/mode_run} 0 -radix bin
isim force add {/sram_counter/mode_read} 0 -radix bin
isim force add {/sram_counter/pins} 00000000 -radix bin
isim force add {/sram_counter/trigger_value} 00000001 -radix bin
isim force add {/sram_counter/trigger_mask} 00000001 -radix bin
isim force add {/sram_counter/sample_reg} 0000000000111 -radix bin
isim force add {/sram_counter/mode_clock} 0001 -radix bin
 
isim force add {/sram_counter/setup_reg} 00000001 -radix bin
 
 
isim force add {/sram_counter/trigger} 0 -radix bin
*/
module sram_counter (
  pic_clock,
  osc_clock,
  sram_clock,
  sram_oe,
  sram_address,
  pins,
  mode_setup,
  mode_run,
  mode_read,
  clock_mode,
  done
);
//global
input wire pic_clock;
input wire osc_clock;
input wire [7:0] pins;
input wire mode_setup;
inout wire mode_run;
input wire mode_read;
input wire clock_mode;
 
output wire done;
output reg [14:0] sram_address;
output wire sram_clock;
output wire sram_oe;
//local
reg trigger_match;
wire trigger;
reg [15:0] sample_counter;//holds trigger and sample counter settings
//SUMP protocol gives samples in /4,
//so sample reg (in setup reg) use two bits less than sample_counter
reg [28:0] setup_reg; //all registers in one big one works best
 
reg [2:0] bit_cnt;
reg bit_out;
 
assign master_clock=(clock_mode)?osc_clock:pic_clock;
 
assign trigger=(((pins[7:0] ^ setup_reg[7:0]) & setup_reg[15:8])==0);
 
assign done=(sample_counter[15]==0); //we are done when the top bit of sample counter flips to 0
 
assign sram_clock=(!done)?master_clock:1'b0;
assign sram_oe=(mode_read)?master_clock:1'b0;
assign mode_run=(mode_read)?bit_out:1'bz;

initial
begin
   sram_address=15'b000000000000000;
   sample_counter=16'b1000000000001111;
   trigger_match=1'b0;
end
 
always @ (posedge master_clock)
begin
   if(mode_setup) begin
      setup_reg={setup_reg[27:0], mode_read}; //in setup mode reuse bit 3 as SDI
   end   
   else if(mode_run) begin
      sample_counter[15]<=1'b1;
      sample_counter[14:2]<=setup_reg[28:16]; //must use this half-step or there are pterm issues
      sample_counter[1:0]<=2'b00;
      trigger_match<=1'b0;
		bit_cnt<=3'b111;
   end
   else if (mode_read) begin
     bit_out = pins[bit_cnt];
     if (bit_cnt == 3'b000) begin
       bit_cnt <= 3'b111;
       sram_address <= sram_address - 1; //down count
     end
     else begin
       bit_cnt <= bit_cnt - 1; // down bit count (MSB first)
     end
   end
   else if(!done) begin
      sram_address <= sram_address + 1; //up count
      if (trigger_match || trigger) begin
         trigger_match<=1'b1;
         sample_counter<=sample_counter-1;
      end
   end
end
 
endmodule // End of Module counter