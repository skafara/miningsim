import re


MESSAGE_INVALID_LOG_FORMAT = "Invalid log format"
"""Message of invalid log format"""


class SimulationLogParsingException(Exception):
    """Represents an exception regarding simulation log parsing."""

    def __init__(self, message: str):
        """Creates an exception with provided message.

        Args:
            message: Message
        """
        super().__init__(message)


class SimulationData:
    """Represents minimal reconstructed simulation data.
    Used for later simulation statistics generation."""

    def __init__(self):
        """Creates empty reconstructed simulation data."""
        self.timestamp_begin = None
        self.timestamp_last_event = None
        self.blocks_mined_cnt = 0
        self.workers_resources_mined = dict()
        self.workers_work_duration = dict()
        self.ferry_wait_durations = list()
        self.lorries_load_duration = list()
        self.lorries_transport_duration = dict()

    def set_timestamp_begin(self, timestamp: int):
        """Sets timestamp of the beginning of the simulation.

        Args:
            timestamp: Timestamp of the beginning of the simulation
        """
        self.timestamp_begin = timestamp

    def update_timestamp_last_event(self, timestamp: int):
        """Updates timestamp of the last event that happened during simulation.

        Args:
            timestamp: Timestamp of the last event yet reached during reconstruction
        """
        self.timestamp_last_event = timestamp

    def inc_blocks_mined_cnt(self):
        """Increments blocks mined count."""
        self.blocks_mined_cnt += 1

    def inc_resources_mined_cnt(self, worker_id: int):
        """Increments the count of the resources mined by the worker with provided ID.

        Args:
            worker_id: Worker ID
        """
        self.workers_resources_mined.setdefault(worker_id, 0)
        self.workers_resources_mined[worker_id] += 1

    def increase_worker_work_duration(self, worker_id: int, duration: int):
        """Increases the duration of the worker working.

        Args:
            worker_id: Worker ID
            duration: Duration
        """
        self.workers_work_duration.setdefault(worker_id, 0)
        self.workers_work_duration[worker_id] += duration

    def register_ferry_wait_duration(self, duration: int):
        """Registers ferry wait duration.

        Args:
            duration: Duration
        """
        self.ferry_wait_durations.append(duration)

    def register_lorry_load_duration(self, duration: int):
        """Registers lorry load duration.

        Args:
            duration: Duration
        """
        self.lorries_load_duration.append(duration)

    def increase_lorry_transport_duration(self, lorry_id: int, duration: int):
        """Increases lorry transport duration.

        Args:
            lorry_id: Lorry ID
            duration: Duration
        """
        self.lorries_transport_duration.setdefault(lorry_id, 0)
        self.lorries_transport_duration[lorry_id] += duration


class SimulationLogParser:
    """Represents a parser of a simulation log file."""

    FOREMAN_LOG_PATTERN = r"(\d+) Foreman -1 Finished analysing the input file.;blocks=(\d+),resources=(\d+)"
    """Pattern to match first log of the file logged by the foreman."""
    GENERAL_LOG_PATTERN = r"(\d+) ([a-zA-Z]+) (-?\d+) (.+\.);duration=(\d+)"
    """Pattern to match all other logs."""

    def __init__(self, file_name: str):
        """Creates a parser of a simulation log file.

        Args:
            file_name: File name
        """
        self.file_name = file_name
        self.data = None

    def parse(self) -> SimulationData:
        """Parses the provided file.

        Returns:
             Minimal reconstructed simulation data
        """
        self.data = SimulationData()
        with open(self.file_name) as file:
            lines = iter(file)

            # parse foreman log
            line = next(lines)
            self.parse_foreman_log(line)

            # parse other logs
            for line in lines:
                self.parse_general_log(line)

            return self.data

    def parse_foreman_log(self, log: str):
        """Parses the first log of the file logged by the foreman and updates reconstructed simulation data.

        Args:
            log: First log of the file logged by the foreman
        """
        match = re.match(self.FOREMAN_LOG_PATTERN, log)
        if match is None:
            raise SimulationLogParsingException(MESSAGE_INVALID_LOG_FORMAT)
        timestamp = match.group(1)
        self.data.set_timestamp_begin(int(timestamp))

    def parse_general_log(self, log: str):
        """Parses general type of a log and updates reconstructed simulation data.

        Args:
            log: General log
        """
        match = re.match(self.GENERAL_LOG_PATTERN, log)
        if match is None:
            raise SimulationLogParsingException(MESSAGE_INVALID_LOG_FORMAT)

        timestamp, role, thread_id, message, duration = match.groups()
        self.handle_log(int(timestamp), role, int(thread_id), message, int(duration))

    def handle_log(self, timestamp: int, role: str, thread_id: int, message: str, duration: int):
        """Based on log values updates reconstructed simulation data.

        Args:
            timestamp: Timestamp
            role: Role
            thread_id: Thread ID
            message: Message
            duration: Duration
        """
        if role == "Worker":
            if message == "Finished mining a resource.":
                self.data.inc_resources_mined_cnt(thread_id)
                self.data.increase_worker_work_duration(thread_id, duration)
            elif message == "Finished mining a block of resources.":
                self.data.inc_blocks_mined_cnt()
        elif role == "Lorry":
            if message == "Lorry has been filled.":
                self.data.register_lorry_load_duration(duration)
            elif message == "Lorry has arrived at the ferry." or message == "Lorry has arrived at the destination.":
                self.data.increase_lorry_transport_duration(thread_id, duration)
        elif role == "Ferry":
            if message == "Ferry has departed from the origin shore.":
                self.data.register_ferry_wait_duration(duration)

        self.data.update_timestamp_last_event(timestamp)


def parse_simulation_log_file(file_name: str) -> SimulationData:
    """Parses simulation log file.

    Args:
        file_name: File name
    Returns:
        Minimal reconstructed simulation data
    """
    return SimulationLogParser(file_name).parse()
