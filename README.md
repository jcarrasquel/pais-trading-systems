<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
</head>
<body>
<h2>Process Mining on Trading Systems - Research Project Repository</h2>
<h5><i>Welcome to our repository! Here you will find software tools and resources related to our research project!</i></h5>
<hr>
<b>Project Researcher:</b> <a href="https://www.hse.ru/en/staff/jcarrasquel">Julio Cesar Carrasquel</a> (Researcher and PhD Candidate in Computer Science)<br>
<br>
<p><img src="https://raw.githubusercontent.com/jcarrasquel/pais-trading-systems/master-2/misc/logo-hse.png" alt="PAIS" width="27" height="37"> <img src="https://raw.githubusercontent.com/jcarrasquel/pais-trading-systems/master-2/misc/logo-pais.png" alt="PAIS" width="37" height="37"></p>
<p><h6>National Research University Higher School of Economics<br>
Laboratory of Process-Aware Information Systems (PAIS Lab). Moscow, Russia.</h6></p>
For contact, please send an email to: <a href = "mailto: jcarrasquel@hse.ru">jcarrasquel@hse.ru</a>
<hr>
<i>Each of the following sections describe the tools and resources in the repository folders.</i> 
<i>For some tools, you may either download the source code and compile, or to download directly the executables as jar files.</i><br>

<b>Index:</b><br>
<ol>
  <li><a href="#pre-processing"><b>Pre-processing</b><a></li>
  <li><a href="#simulation"><b>Simulation</b><a></li>
  <li><a href="#event_logs"><b>Event Logs</b><a></li>
  <li><a href="#models"><b>Models</b><a></li>
</ol>
<hr>
<h3 name="pre-processing"><i>1. Pre-processing</i></h3>
We have implemented two pre-processing tools for extracting event logs for process mining from a capture file (in pcap format) of FIX messages; in this folder, you will find the following command-line programs:
<ul>
  <br>
  <li><b>eventlog-gen-orders:</b> It generates an event log from the FIX messages such that each case is the observed trace for an order. In the current version, all orders in this event log are trading the same financial security. As input it takes the file path for the captured pcap file of FIX messages:<br>
    <i>usage example: java -jar eventlog-gen fix_messages.pcap securityId </i></li> <br>
  <li><b>eventlog-gen-orderbooks:</b> It generates an event log from the FIX messages such that each case is the trading session in an order book. Each order book is associated with the trading of a single financial security. The program takes as input it takes the file path for the captured pcap file of FIX messages, and a file containing the list of financial securities (one per line)<br>
    <i>usage example: java -jar eventlog-gen fix_messages.pcap security_list.txt </i></li> 
</ul>
  
In the folder <b>event logs</b> you also can find examples of the event logs than these pre-processing tools generate.
<hr>
<h3 name="simulation"><i>2. Simulation</i></h3>
<ul>
  <br>
  <li> <b>orderbook-interface:</b> The order book interface is a prototype for replay and simulation support, providing a convenient visualization of the order book states. It works in two modes. It may either read an order book event log, i.e, generated with the tool eventlog-gen-orderbooks, or it may read events in stream from a socket connection given some host and port of the other pair. In the following we present two usage examples for executing this tool:<br>
    <br>
    <ul>
      <li><i>File mode: java -jar orderbook-interface.jar -f order-book-eventlog.csv </i></li>
      <li><i>Socket mode: java -jar orderbook-interface.jar -s host_ipaddress port_number </i></li>
    </ul>
  </li>
</ul>
<hr>
<h3 name="event_logs"><i>3. Event Logs</i></h3>
In this folder you will find examples of events logs generated using the eventlog-gen-orders and eventlog-gen-orderbooks pre-processing tools.
<hr>
<h3 name="models"><i>4. Models</i></h3>
In this section we store formal models of the system that aim to describe the functioning of the trading system components. Our research goal is to relate these models with the event logs (which describe the system observed behavior) in order to analyze possible deviations of the system.
<ul>
  <br>
  <li> <b>CPN:</b> This folder contains coloured Petri net models designed with CPN Tools. 
</ul>
</body>
</html>
