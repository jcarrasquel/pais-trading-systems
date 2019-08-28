<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
</head>
<body>
<h2>Process Mining on Trading Systems - Research Project Repository</h2>
<h5><i>Welcome to our repository! Here you will find software tools and resources related to our research project!</i></h5>
<i>Each of the following sections describe the tools and resources in the repository folders.</i><br>
<i>You may either download the source code of our tools and compile, or download directly the executable tools as jar files.</i><br>
<hr>
<b>Project Researcher:</b> <a href="https://www.hse.ru/en/staff/jcarrasquel">Julio C. Carrasquel</a><br>
For contact, please send an email to: <a href = "mailto: jcarrasquel@hse.ru">jcarrasquel@hse.ru</a><br>
  
<p><img src="https://raw.githubusercontent.com/jcarrasquel/pais-trading-systems/master-2/misc/logo-hse.png" alt="PAIS" width="27" height="37"> <img src="https://raw.githubusercontent.com/jcarrasquel/pais-trading-systems/master-2/misc/logo-pais.png" alt="PAIS" width="37" height="37">
<h6>National Research University Higher School of Economics<br>
Laboratory of Process-Aware Information Systems (PAIS Lab). Moscow, Russia.</h6></p>
<hr>
<h3><i>Pre-processing</i></h3>
We have implemented two pre-processing tools for extracting event logs for process mining from a capture file (in pcap format) of FIX messages; in this folder, you will find the following command-line programs:
<ul>
  <br>
  <li><b>eventlog-gen-orders:</b> Generates an event log from the FIX messages such that each case is the observed trace for an order. In the current version, all orders in this event log are trading the same financial security. As input it takes the file path for the captured pcap file of FIX messages:<br>
    <i>usage example: java -jar eventlog-gen fix_messages.pcap securityId </i></li> <br>
  <li><b>eventlog-gen-orderbooks:</b> Generates an event log from the FIX messages such that each case is the trading session in an order book. Each order book is associated with the trading of a single financial security. The program takes as input it takes the file path for the captured pcap file of FIX messages, and a file containing the list of financial securities (one per line)<br>
    <i>usage example: java -jar eventlog-gen fix_messages.pcap security_list.txt </i></li> 
</ul>
  
In the folder <b>event logs</b> you also can find examples of the event logs than these pre-processing tools generate.
<hr>
<h3><i>Simulation</i></h3>
<ul>
  <br>
  <li> <b>orderbook-interface:</b> the order book interface is a prototype for replay and simulation support, providing a convenient visualization of the order book states. It works in two modes. It may either read an order book event log, i.e, generated with the tool eventlog-gen-orderbooks, or it may read events in stream from a socket connection given some host and port of the other pair. In the following we present two usage examples for executing this tool:
    <br>
    <ul>
      <li><i>File mode: java -jar orderbook-interface.jar -f order-book-eventlog.csv </i></li>
      <br>
      <li><i>Socket mode: java -jar orderbook-interface.jar -s host_ipaddress port_number </i></li>
    </ul>
  </li>
</ul>
</body>
</html>
