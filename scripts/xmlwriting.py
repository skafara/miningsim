import xml.dom.minidom

import statistics


class SimulationStatisticsDocumentWriter:
    """Represents an XML Document Writer of provided simulation statistics data."""

    def __init__(self, stats: statistics.SimulationStatistics):
        """Creates an XML Document Writer.

        Args:
            stats: Simulation statistics to be later written to XML Document
        """
        self.STATS = stats
        self.doc_gen = xml.dom.minidom.Document()

    def write(self) -> xml.dom.minidom.Document:
        """Writes provided simulation statistics data to an XML document.

        Returns:
            XML document
        """
        doc = xml.dom.minidom.Document()
        doc.appendChild(self.get_simulation())
        return doc

    def create_element(self, tag_name: str, attrs: list = None, inner_elements: list = None) -> xml.dom.minidom.Element:
        """Creates an XML element of provided tag name, with provided attributes and inner elements.

        Args:
            tag_name: Tag name
            attrs: List of (attribute, value) pairs
            inner_elements: List of inner elements of the element
        Returns:
            XML Element
        """
        element = self.doc_gen.createElement(tag_name)

        if attrs is not None:
            for attr, value in attrs:
                element.setAttribute(attr, str(value))

        if inner_elements is not None:
            for inner_element in inner_elements:
                element.appendChild(inner_element)

        return element

    def create_text_node(self, data) -> xml.dom.Node:
        """Creates an XML text node with provided text.

        Args:
            data: Text of the node
        Returns:
            XML text node
        """
        return self.doc_gen.createTextNode(str(data))

    def get_simulation(self) -> xml.dom.minidom.Element:
        """Creates XML Simulation element.

        Returns:
            XML Simulation Element
        """
        return self.create_element(
            "Simulation",
            attrs=[("duration", self.STATS.DURATION)],
            inner_elements=[
                self.get_block_avg_duration(),
                self.get_resource_avg_duration(),
                self.get_ferry_avg_wait(),
                self.get_workers(),
                self.get_vehicles()
            ]
        )

    def get_block_avg_duration(self) -> xml.dom.minidom.Element:
        """Creates XML blockAverageDuration element.

        Returns:
            XML blockAverageDuration Element
        """
        return self.create_element(
            "blockAverageDuration",
            attrs=[("totalCount", self.STATS.BLOCKS_MINED_CNT)],
            inner_elements=[self.create_text_node(self.STATS.AVG_BLOCK_MINE_DURATION)]
        )

    def get_resource_avg_duration(self) -> xml.dom.minidom.Element:
        """Creates XML resourceAverageDuration element.

        Returns:
            XML resourceAverageDuration Element
        """
        return self.create_element(
            "resourceAverageDuration",
            attrs=[("totalCount", self.STATS.RESOURCES_MINED_CNT)],
            inner_elements=[self.create_text_node(self.STATS.AVG_RESOURCE_MINE_DURATION)]
        )

    def get_ferry_avg_wait(self) -> xml.dom.minidom.Element:
        """Creates XML ferryAverageWait element.

        Returns:
            XML ferryAverageWait Element
        """
        return self.create_element(
            "ferryAverageWait",
            attrs=[("trips", self.STATS.FERRY_TRIPS_CNT)],
            inner_elements=[self.create_text_node(self.STATS.AVG_FERRY_WAIT_DURATION)]
        )

    def get_workers(self) -> xml.dom.minidom.Element:
        """Creates XML Workers element.

        Returns:
            XML Workers Element
        """
        return self.create_element(
            "Workers",
            inner_elements=[
                self.get_worker(worker_stats) for worker_stats in self.STATS.WORKERS_STATISTICS
            ]
        )

    def get_worker(self, worker_stats: statistics.WorkerStatistics) -> xml.dom.minidom.Element:
        """Creates XML Worker element with provided worker statistics.

        Args:
            worker_stats: Worker statistics

        Returns:
            XML Worker Element
        """
        return self.create_element(
            "Worker",
            attrs=[("id", worker_stats.WORKER_ID)],
            inner_elements=[
                self.create_element(
                    "resources",
                    inner_elements=[self.create_text_node(worker_stats.RESOURCES_MINED_CNT)]
                ),
                self.create_element(
                    "workDuration",
                    inner_elements=[self.create_text_node(worker_stats.WORK_DURATION)]
                )
            ]
        )

    def get_vehicles(self) -> xml.dom.minidom.Element:
        """Creates XML Vehicles element.

        Returns:
            XML Vehicles Element
        """
        return self.create_element(
            "Vehicles",
            inner_elements=[
                self.get_vehicle(lorry_stats) for lorry_stats in self.STATS.LORRIES_STATISTICS
            ]
        )

    def get_vehicle(self, lorry_stats: statistics.LorryStatistics) -> xml.dom.minidom.Element:
        """Creates XML Vehicle element with provided lorry statistics.

        Args:
            lorry_stats: Lorry statistics

        Returns:
            XML Vehicle Element
        """
        return self.create_element(
            "Vehicle",
            attrs=[("id", lorry_stats.LORRY_ID)],
            inner_elements=[
                self.create_element(
                    "loadTime",
                    inner_elements=[self.create_text_node(lorry_stats.LOAD_TIME)]
                ),
                self.create_element(
                    "transportTime",
                    inner_elements=[self.create_text_node(lorry_stats.TRANSPORT_TIME)]
                )
            ]
        )


def make_simulation_statistics_document(stats: statistics.SimulationStatistics) -> xml.dom.minidom.Document:
    """Makes an XML Document from provided simulation statistics.

    Args:
        stats: Simulation statistics
    Returns:
        XML Document
    """
    return SimulationStatisticsDocumentWriter(stats).write()
