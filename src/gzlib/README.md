This source folder contains the files to build a small library to install on the Raspberry Pi. The library is
installed to /usr/local/lib/libgz.a. The headers are installed to /usr/local/include. The library is bound with
the -lgz switch.

For Python fans, there is a lbrary module, gz_libpy.c which provides Python bindings for the library. You install
it by running 'python setup.py install'.

There are Python versions of both the test and demonstration code. Note that the many of the demo code examples
rely on the curses module which is installed with python2.7 and python3.2 libraries.
