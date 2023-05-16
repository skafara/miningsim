# miningsim
Mining Process Simulation

Seminar Work of KIV/PGS - "Programming structures"

> University of West Bohemia, Pilsen

## Compilation

Bundle a `jar` by running provided script.

`bash packjar.sh`

## Usage

`java -jar app.jar -i <in-file> -o <out-file> -cWorker <c-worker> -tWorker <t-worker> -capLorry <cap-lorry> -tLorry <t-lorry> -capFerry <cap-ferry>`

	<in-file>   - Path to input data file
	<out-file>  - Path to output log file
	<c-worker>  - Workers count
	<t-worker>  - Time [ms] it takes a worker to mine a resource
	<cap-lorry> - Resources count a lorry can load and transport
	<t-lorry>   - Time [ms] it takes a lorry to transport loaded resources
	<cap-ferry> - Lorries count a ferry can load and transport

`python3 scripts/app.py -i <in-file> -o <out-file>`

	<in-file>   - Path to input log file
	<out-file>  - Path to XML output statistics file

## Example

`java -jar app.jar -i data/ref_input.txt -o log.txt -cWorker 4 -tWorker 50 -capLorry 1000 -tLorry 1000 -capFerry 4`

`python3 scripts/app.py -i log.txt -o statistics.xml`

	Simulation is launched with provided parameters (~45 min).
	Events of simulation are logged into a file.
	Simulation data is reconstructed from the logs.
	Statistics are written as an XML document to a file.
