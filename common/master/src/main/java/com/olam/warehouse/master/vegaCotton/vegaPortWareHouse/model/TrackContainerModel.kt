package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao.TrackContainerDao
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.TrackContainer

class TrackContainerModel(private val dao: TrackContainerDao) {

    fun addContainer(container: TrackContainer) = dao.addContainer(container)

    fun isContainerExist(containerNumber: String) = dao.isContainerExist(containerNumber)

    fun getContainers() = dao.getContainers()

    fun getContainersAsList() = dao.getContainersAsList()

    fun deleteTrackContainers() = dao.deleteTrackContainers()
}
