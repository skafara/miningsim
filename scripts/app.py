import sys
import getopt
import xml.dom.minidom

import parsing
import statistics
import xmlwriting


PATTERN_MESSAGE = "miningsim[%s]: %s"
"""Application message pattern"""

PATTERN_ERROR_MESSAGE = PATTERN_MESSAGE % ("ERROR", "%s")
"""Application error message pattern"""

PATTERN_INFO_MESSAGE = PATTERN_MESSAGE % ("INFO", "%s")
"""Application info message pattern"""

DIVIDER = "--------------------------------------------------"
"""Console print divider"""


def print_app_info():
    """Prints info about the application."""
    print("miningsim - Mining Simulator (2023-04-16, v1)")
    print(DIVIDER)
    print("Seminar Work of KIV/PGS - \"Programming Structures\"")
    print("Stanislav Kafara, skafara@students.zcu.cz")
    print("University of West Bohemia, Pilsen")
    print(DIVIDER)


def print_info_message(message: str):
    """Prints informative message.

    Args:
        message: Message
    """
    print(PATTERN_INFO_MESSAGE % message)


def print_error_message(message: str):
    """Prints error message to stderr.

    Args:
        message: Message
    """
    print(PATTERN_ERROR_MESSAGE % message, file=sys.stderr)


def extract_input_arguments(argv: list) -> tuple:
    """Extracts program input arguments.

    Args:
        argv: List of program arguments
    Returns:
        Input file name, Output file name
    Raises:
        getopt.GetoptError: If there are unknown or not enough arguments or if the arguments are not valid.
    """
    opts, args = getopt.getopt(argv, "i:o:")

    if len(args) > 0:
        raise getopt.GetoptError("Unknown arguments")

    in_file_name, out_file_name = None, None
    for opt, arg in opts:
        if opt == "-i":
            in_file_name = arg
        elif opt == "-o":
            out_file_name = arg

    if in_file_name is None or out_file_name is None:
        raise getopt.GetoptError("Not enough arguments")

    return in_file_name, out_file_name


def write_xml_doc_to_file(doc: xml.dom.minidom.Document, file_name: str):
    """Writes an XML Document to a file.

    Args:
        doc: XML Document
        file_name: File name
    """
    with open(file_name, "w") as file:
        doc.writexml(file)


def main():
    """Application entry point.
    1) Extracts application input arguments.
    2) Reconstructs minimal simulation data.
    3) Makes statistics of them.
    4) Makes XML Document of them.
    5) Writes it to an output file."""
    try:
        print_app_info()
        in_file_name, out_file_name = extract_input_arguments(sys.argv[1:])
        print_info_message("Extracted application input arguments")
        data = parsing.parse_simulation_log_file(in_file_name)
        print_info_message(f"Parsed input simulation log file '{in_file_name}'")
        print_info_message("Reconstructed simulation data from logged events")
        stats = statistics.make_simulation_statistics(data)
        print_info_message("Created simulation statistics")
        document = xmlwriting.make_simulation_statistics_document(stats)
        print_info_message("Statistics written to XML Document")
        write_xml_doc_to_file(document, out_file_name)
        print_info_message(f"XML Document written to output file '{out_file_name}'")
    except getopt.GetoptError as e:
        print_error_message(e)
        print("[EXAMPLE]: $ python3 run_sp2.py -i <in_file_name> -o <out_file_name>")
    except FileNotFoundError as e:
        print_error_message(e)
    except Exception as e:
        print_error_message(f"Unexpected error occurred during program execution: {e}")
    else:
        print_info_message("OK - ALL DONE")


if __name__ == '__main__':
    main()
