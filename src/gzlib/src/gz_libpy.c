/*
 * gz_libpy.c
 * 
 * Copyright 2013  guzunty
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * 
 * This program provides a Python wrapper for the Guzunty library.
 * To use it,  import it with:
 * import GZ
 * 
 */

#include <Python.h>
#include <gz_clk.h>
#include <gz_spi.h>

static PyObject* clock_ena(PyObject* self, PyObject* args)
{
  int speed;
  int divisor;
  if( !(PyArg_ParseTuple(args, "") || PyArg_ParseTuple(args, "ii", &speed, &divisor))) {
    return NULL;
  }
  PyErr_Clear();
  gz_clock_ena(speed, divisor);
  Py_RETURN_NONE;
}

static PyObject* clock_dis(PyObject* self, PyObject* args)
{
  PyErr_Clear();
  gz_clock_dis();
  Py_RETURN_NONE;
}

static PyObject* spi_set(PyObject* self, PyObject* args)
{
  int bit_to_set;
  if( !(PyArg_ParseTuple(args, "") || PyArg_ParseTuple(args, "i", &bit_to_set))) {
    return NULL;
  }
  PyErr_Clear();
  gz_spi_set(bit_to_set);
  Py_RETURN_NONE;
}

static PyObject* spi_reset(PyObject* self, PyObject* args)
{
  int bit_to_reset;
  if( !(PyArg_ParseTuple(args, "") || PyArg_ParseTuple(args, "i", &bit_to_reset))) {
    return NULL;
  }
  PyErr_Clear();
  gz_spi_reset(bit_to_reset);
  Py_RETURN_NONE;
}

static PyObject* spi_write(PyObject* self, PyObject* args)
{
  long to_write = 0;
  if( !(PyArg_ParseTuple(args, "") || PyArg_ParseTuple(args, "l", &to_write))) {
    return NULL;
  }
  PyErr_Clear();
  gz_spi_write((unsigned char *)&to_write);
  Py_RETURN_NONE;
}

static PyObject* spi_read(PyObject* self, PyObject* args)
{
  long read = 0;
  PyErr_Clear();
  gz_spi_read((unsigned char*)&read);
  return Py_BuildValue("l", read);
}

static PyObject* spi_get(PyObject* self, PyObject* args)
{
  int bit_to_read;
  if( !(PyArg_ParseTuple(args, "") || PyArg_ParseTuple(args, "i", &bit_to_read))) {
    return NULL;
  }
  PyErr_Clear();
  int read = gz_spi_get(bit_to_read);
  return Py_BuildValue("i", read);
}

static PyObject* output_get(PyObject* self, PyObject* args)
{
  int bit_to_read;
  if( !(PyArg_ParseTuple(args, "") || PyArg_ParseTuple(args, "i", &bit_to_read))) {
    return NULL;
  }
  PyErr_Clear();
  int read = gz_output_get(bit_to_read);
  return Py_BuildValue("i", read);
}

static PyObject* spi_set_width(PyObject* self, PyObject* args)
{
  int width;
  if( !(PyArg_ParseTuple(args, "") || PyArg_ParseTuple(args, "i", &width))) {
    return NULL;
  }
  PyErr_Clear();
  gz_spi_set_width(width);
  Py_RETURN_NONE;
}

static PyObject* spi_close(PyObject* self, PyObject* args)
{
  PyErr_Clear();
  gz_spi_close();
  Py_RETURN_NONE;
}

static PyMethodDef gzMethods[] =
{
  {"clock_ena", clock_ena, METH_VARARGS, "Enabling clock"},
  {"clock_dis", clock_dis, METH_NOARGS, "Disabling clock"},
  {"spi_set", spi_set, METH_VARARGS, "Setting output"},
  {"spi_reset", spi_reset, METH_VARARGS, "Resetting output"},
  {"spi_write", spi_write, METH_VARARGS, "Writing SPI output"},
  {"spi_read", spi_read, METH_NOARGS, "Reading SPI input"},
  {"spi_get", spi_get, METH_VARARGS, "Bitwise SPI read"},
  {"output_get", output_get, METH_VARARGS, "Bitwise read of SPI output buffer"},
  {"spi_set_width", spi_set_width, METH_VARARGS, "Setting SPI width in bytes"},
  {"spi_close", spi_close, METH_NOARGS, "Closing SPI device"},
  {NULL, NULL, 0, NULL}
};

PyMODINIT_FUNC

initGZ(void)
{
  PyObject* module;
  PyObject* dictionary;
  PyObject* constant;
  
  module = Py_InitModule("GZ", gzMethods);
  dictionary = PyModule_GetDict(module);
  
  constant = PyInt_FromLong(GZ_CLK_5MHz);
  PyDict_SetItemString(dictionary, "GZ_CLK_5MHz", constant);
  Py_DECREF(constant);

  constant = PyInt_FromLong(GZ_CLK_125MHz);
  PyDict_SetItemString(dictionary, "GZ_CLK_125MHz", constant);
  Py_DECREF(constant);
}
