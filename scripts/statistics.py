import parsing


class WorkerStatistics:
    """Represents statistics of a worker."""

    def __init__(self, worker_id: int, resources_mined_cnt: int, work_duration: int):
        """Creates statistics of a worker.

        Args:
            worker_id: Worker ID
            resources_mined_cnt: Resources mined count
            work_duration: Work duration
        """
        self.WORKER_ID = worker_id
        self.RESOURCES_MINED_CNT = resources_mined_cnt
        self.WORK_DURATION = work_duration


class LorryStatistics:
    """Represents statistics of a lorry."""

    def __init__(self, lorry_id: int, load_time: int, transport_time: int):
        """Creates statistics of a lorry.

        Args:
            lorry_id: Lorry ID
            load_time: Load time
            transport_time: Transport time
        """
        self.LORRY_ID = lorry_id
        self.LOAD_TIME = load_time
        self.TRANSPORT_TIME = transport_time


class SimulationStatistics:
    """Represents statistics of a simulation."""

    def __init__(self, duration: int,
                 blocks_mined_cnt: int, avg_block_mine_duration: int,
                 resources_mined_cnt: int, avg_resource_mine_duration: int,
                 ferry_trips_cnt: int, avg_ferry_wait_duration: int,
                 workers_statistics: list, lorries_statistics: list):
        """Creates statistics of a simulation.

        Args:
            duration: Total simulation duration
            blocks_mined_cnt: Blocks mined count
            avg_block_mine_duration: Average block mine duration
            resources_mined_cnt: Resources mined count
            avg_resource_mine_duration: Average resource mine duration
            ferry_trips_cnt: Ferry trips count
            avg_ferry_wait_duration: Average ferry wait duration
            workers_statistics: Workers statistics
            lorries_statistics: Lorries statistics
        """
        self.DURATION = duration

        self.BLOCKS_MINED_CNT = blocks_mined_cnt
        self.AVG_BLOCK_MINE_DURATION = avg_block_mine_duration

        self.RESOURCES_MINED_CNT = resources_mined_cnt
        self.AVG_RESOURCE_MINE_DURATION = avg_resource_mine_duration

        self.FERRY_TRIPS_CNT = ferry_trips_cnt
        self.AVG_FERRY_WAIT_DURATION = avg_ferry_wait_duration

        self.WORKERS_STATISTICS = workers_statistics
        self.LORRIES_STATISTICS = lorries_statistics


def make_workers_statistics(data: parsing.SimulationData) -> list:
    """Makes workers statistics from provided simulation data.

    Args:
        data: Simulation data
    Returns:
        List of workers statistics
    """
    return sorted([
        WorkerStatistics(
            worker_id,
            data.workers_resources_mined[worker_id],
            data.workers_work_duration[worker_id]
        )
        for worker_id in data.workers_resources_mined.keys()
    ], key=lambda ws: ws.WORKER_ID)


def make_lorries_statistics(data: parsing.SimulationData, avg_ferry_wait_duration: int) -> list:
    """Makes lorries statistics from provided simulation data.

    Args:
        data: Simulation data
        avg_ferry_wait_duration: Average ferry wait duration
    Returns:
        List of lorries statistics
    """
    return sorted([
        LorryStatistics(
            lorry_id,
            load_duration,
            data.lorries_transport_duration[lorry_id] + avg_ferry_wait_duration
        )
        for lorry_id, load_duration in enumerate(data.lorries_load_duration)
    ], key=lambda ls: ls.LORRY_ID)


def make_simulation_statistics(data: parsing.SimulationData) -> SimulationStatistics:
    """Makes simulation statistics from provided simulation data.

    Args:
        data: Simulation data
    Returns:
        Simulation statistics
    """
    duration = data.timestamp_last_event - data.timestamp_begin

    sum_workers_work_duration = sum(data.workers_work_duration.values())
    avg_block_mine_duration = sum_workers_work_duration / data.blocks_mined_cnt

    resources_mined_cnt = sum(data.workers_resources_mined.values())
    avg_resource_mine_duration = sum_workers_work_duration / resources_mined_cnt
    ferry_trips_cnt = len(data.ferry_wait_durations)
    avg_ferry_wait_duration = sum(data.ferry_wait_durations) / ferry_trips_cnt

    workers_statistics = make_workers_statistics(data)
    lorries_statistics = make_lorries_statistics(data, avg_ferry_wait_duration)

    return SimulationStatistics(
        duration,
        data.blocks_mined_cnt,
        avg_block_mine_duration,
        resources_mined_cnt,
        avg_resource_mine_duration,
        ferry_trips_cnt,
        avg_ferry_wait_duration,
        workers_statistics,
        lorries_statistics
    )
