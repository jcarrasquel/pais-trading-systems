<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
</head>
<body>
<h2>Process Mining on Trading Systems - Research Project Repository</h2>
<h5><i>Welcome to our repository! Here you will find software tools and resources related to our research project!</i></h5>
<hr>
<b>Project Researcher:</b> <a href="https://www.hse.ru/en/staff/jcarrasquel">Julio Cesar Carrasquel</a> (Research Assistant | PhD Candidate)<br>
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
We have implemented two pre-processing tools for extracting event logs for process mining from a capture file (in pcap format) of FIX messages; in this folder, you will find the following command-line programs:<br>
<ul>
  <br>
  <li><b>eventlog-gen-orders:</b> It generates an event log from the FIX messages such that each case is the observed trace for an order. In the current version, all orders in this event log are trading the same financial security. For using this program, it takes as input the file path for the captured pcap file of FIX messages:<br>
    <i><b>usage example:</b> java -jar eventlog-gen fix_messages.pcap securityId </i><br> The generated event logs using this program can be synthesized into process models that describe the executed path of orders (see image below). The latter is just an example of the many possiblities for analyzing order behavior based on these event logs.<br>
    <img alt="order_trace" src="https://raw.githubusercontent.com/jcarrasquel/pais-trading-systems/master-2/misc/researchpage_order_trace.png" width="594" height="84"></li> <br>
  
  <li><b>eventlog-gen-orderbooks:</b> It generates an event log from the FIX messages such that each case is the trading session in an order book. Each order book is associated with the trading of a single financial security. The program takes as input it takes the file path for the captured pcap file of FIX messages, and a file containing the list of financial securities (one per line)<br>
    <i><b>usage example:</b> java -jar eventlog-gen fix_messages.pcap security_list.txt </i><br>These kind of event logs may be replayed in the interface for replay and simulation described below.                                                                                              </li>
    <br>
  <li><b>eventlog-orderbooks-directory:</b> It generates a <i>directory of order books</i> from the FIX messages. Specifically, it generates an output file in which each line denoting the identifier of the financial security being traded, the number of events processed related to the trading of such security, and the number of orders involved in the trading. We call it a directory of order books, since we assume a relationship 1:1 between a security and an order book. Notice that this is not an event log! However, the security identifiers given in this file can be used as input for the event log generators describe above. This program can be executed as follows:<br>
    <i><b>usage example:</b> java -jar eventlog-orderbooks-directory fix_messages.pcap</i></li> <br>
</ul>
  
In the folder <b>event logs</b> you also can find examples of the event logs and other related output files than these pre-processing tools generate.

<hr>
<h3 name="simulation"><i>2. Replay and Simulation</i></h3>
<ul>
  <br>
  <li> <b>orderbook-interface:</b> The order book interface is a prototype for replay and simulation support, providing a convenient visualization of the order book states (see image below). It works in two modes. It may either read an order book event log, i.e, generated with the tool eventlog-gen-orderbooks, or it may read events in stream from a socket connection given some host and port of the other pair. In the current version it supports the replay of a single order book. In the following, we present two usage examples for executing this tool:<br>
    <br>
    <ul>
      <li><i>File mode: java -jar orderbook-interface.jar -f order-book-eventlog.csv </i></li>
      <li><i>Socket mode: java -jar orderbook-interface.jar -s host_ipaddress port_number </i></li>
    </ul><br>
  <img alt="interface_prototype" src="https://raw.githubusercontent.com/jcarrasquel/pais-trading-systems/master-2/misc/researchpage_interface_prototype.png" width="426" height="138">
  </li>
</ul>
<hr>
<h3 name="event_logs"><i>3. Event Logs</i></h3>
In this folder you will find examples of events logs generated using the eventlog-gen-orders and eventlog-gen-orderbooks pre-processing tools.
<hr>
<h3 name="models"><i>4. Models (Section pending to be added)</i></h3>
In this section we store formal models of the system that aim to describe the functioning of the trading system components. Our research goal is to relate these models with the event logs (which describe the system observed behavior) in order to analyze possible deviations of the system. The models can be also used for simulation purposes.
<ul>
  <br>
  <li> <b>CPN:</b> This folder contains coloured Petri net models designed with CPN Tools. 
</ul>
</body>
</html>
