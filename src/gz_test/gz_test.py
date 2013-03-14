#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  gz_test.py
#  
#  Copyright 2013  guzunty
#  
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#  
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software
#  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
#  MA 02110-1301, USA.
#  
# You need to have built and installed the GZ Guzunty Python extension
# module in gzlib using: "sudo python setup.py install"
#  

import GZ

GZ.clock_ena(GZ.GZ_CLK_5MHz, 0x7f) # Turn on a slow clock
raw_input("Press any key to stop test.")
GZ.clock_dis()

