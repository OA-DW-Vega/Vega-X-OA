package com.olam.warehouse.vegax.portwarehouse.data.repository

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao.DispatchDao
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.portwarehouse.data.api.DirectDispatchApi

/**
 * Created by Baskaran Kannan on 4/1/2021.
 */
interface DirectDispatchRepository {
    suspend fun getMtns(): LiveData<Resource<GenericReqAndResp<List<PortMtn>>>>
    suspend fun updateBalesToDirectDispatch(bales: List<PortMtnBales>): LiveData<Resource<GenericReqAndResp<GenericMessage>>>
    fun getContainersByOtId(otNumber: String): List<Container>
    suspend fun getOTDetails(it: String): LiveData<Resource<GenericReqAndResp<DispatchOT>>>
    suspend fun getContainerDetails(containerNo: String, otNo: String): LiveData<Resource<GenericReqAndResp<Container>>>
    suspend fun getContainerBaleDetails(
        containerNo: String,
        otNo: String
    ): LiveData<Resource<GenericReqAndResp<Container>>>

    suspend fun updateOT(data: DispatchOT)
    fun getOTById(otNumber: String): DispatchOTWithContainers
    fun loadContainerAndBales(containerNo: String, otNumber: String): ContainerWithBales
    suspend fun getOtList(): LiveData<Resource<GenericReqAndResp<List<DispatchOT>>>>
    suspend fun insertOrReplaceOTs(it: List<DispatchOT>)
    fun getOTsLocal(): List<DispatchOT>
    suspend fun insertOrReplaceBales(bales: List<PortBale>)
    suspend fun insertOrReplaceBale(bale: PortBale)
    suspend fun insertOrReplaceContainer(container: Container)
    suspend fun deleteAllBalesByOtId(otNumber: String)
    suspend fun getBaleDeatils(baleId: String): LiveData<PortBale>
    suspend fun deleteContainerFromServer(
        containerNumber: String,
        otNumber: String
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>>

    fun getContainerAndBale(containerId: String, otNumber: String): ContainerWithBales
    fun deleteContainer(containerId: String)
    suspend fun holdStuffing(
        containerNumber: String,
        otNumber: String,
        status: Int
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>>

    suspend fun updateDispatchMode(
        otNumber: String,
        direct: Boolean
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>>

    suspend fun getBaleDetails(
        baleId: String,
        containerId: String,
        otNumber: String?,
        direct: Boolean
    ): LiveData<Resource<GenericReqAndResp<PortBale>>>

    suspend fun deleteBaleDetail(
        baleId: String,
        containerNumber: String
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>>

    fun deleteBaleFromDB(bale: PortBale)
    suspend fun sealContainer(
        sealId: String,
        containerId: String,
        otNumber: String,
        direct: Boolean
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>>

    fun updateSealIDToLocalDB(sealNumber: String, containerNumber: String)
    suspend fun getOTDetailCom(otNumber: String): LiveData<Resource<GenericReqAndResp<DispatchOT>>>
    suspend fun confirmDispatch(otNumber: String): LiveData<Resource<GenericReqAndResp<GenericMessage>>>
    suspend fun doBreakSealContainer(
        otNumber: String,
        containerNumber: String,
        sealNumber: String
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>>
    fun updateOTNumberLocalDB(otNumber: String, containerNumber: String)
    fun updateSplitContainer(isSelected: Boolean, containerNumber: String)
    suspend fun updateOTNumberDispatch(otNumber: String, oldOTNumber: String, containerList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<SplitContainer>>>
}

class DirectDispatchRepositoryImpl(val api: DirectDispatchApi, private val dao: DispatchDao) :
    DirectDispatchRepository {

    override suspend fun getMtns(): LiveData<Resource<GenericReqAndResp<List<PortMtn>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<PortMtn>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<PortMtn>> =
                api.getMtnList(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getBaleDeatils(baleId: String) = dao.getBaleDeatils(baleId)
    override suspend fun deleteAllBalesByOtId(otNumber: String) = dao.deleteAllBalesByOtId(otNumber)
    override suspend fun insertOrReplaceBales(bales: List<PortBale>) = dao.insertOrReplaceBales(bales)
    override suspend fun insertOrReplaceContainer(container: Container) = dao.insertOrReplaceContainer(container)
    override suspend fun updateBalesToDirectDispatch(bales: List<PortMtnBales>): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall(): GenericReqAndResp<GenericMessage> =
                api.updateDirectDispatchMtn(getCurrentKey(), bales)
        }.build().asLiveData()
    }

    override fun getContainersByOtId(otNumber: String): List<Container> = dao.getContainersByOtId(otNumber)
    override suspend fun getOTDetails(it: String): LiveData<Resource<GenericReqAndResp<DispatchOT>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<DispatchOT>>() {
            override suspend fun createCall(): GenericReqAndResp<DispatchOT> =
                api.getOTDetails(getCurrentKey(), it)
        }.build().asLiveData()
    }

    override suspend fun getContainerDetails(
        containerNo: String,
        otNumber: String
    ): LiveData<Resource<GenericReqAndResp<Container>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<Container>>() {
            override suspend fun createCall(): GenericReqAndResp<Container> =
                api.getContainerDetail(
                    getCurrentKey(),
                    Container(containerNumber = containerNo, otNumber = otNumber)
                )
        }.build().asLiveData()
    }

    override suspend fun getContainerBaleDetails(
        containerNo: String,
        otNumber: String
    ): LiveData<Resource<GenericReqAndResp<Container>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<Container>>() {
            override suspend fun createCall(): GenericReqAndResp<Container> =
                api.getContainerBaleDetail(
                    getCurrentKey(),
                    Container(containerNumber = containerNo, otNumber = otNumber)
                )
        }.build().asLiveData()
    }

    /* override suspend fun getContainerDetails(
         containerNo: String,
         otNumber: String
     ): LiveData<Resource<Container>>? {
         return object : NetworkBoundResource<Container, GenericReqAndResp<Container>>() {

             override fun processResponse(response: GenericReqAndResp<Container>): Container=response.data
             /*{
                 val containerWithBales = ContainerWithBales()
                 containerWithBales.container = response.data
                 // val dbData = dao.loadContainerAndBales(containerNo, otNumber)
             //    containerWithBales.bales = dbData.bales
                 return response.data
             }*/

             override suspend fun saveCallResults(items: Container) {
                 items.let { container ->
                     dao.deleteAllBalesByOtId(container.otNumber)
                     dao.insertOrReplaceContainer(container)
                     container.bales?.let {
                         it.forEach { bale ->
                             bale.containerNumber = container.containerNumber
                             bale.otNumber = container.otNumber
                         }
                         dao.insertOrReplaceBales(it)
                     }
                 }
             }

             override fun shouldFetch(data: Container?): Boolean = true

             override suspend fun loadFromDb():Container=dao.loadContainer(containerNo, otNumber)
                     /*Container{
                 var data = Container()
                 data =
                 return data
             }*/
             override suspend fun createCall(): GenericReqAndResp<Container> = api.getContainerDetail(
                 Container(containerNumber = containerNo, otNumber = otNumber)
             )

         }.build().asLiveData()

     }
 */
    override suspend fun updateOT(it: DispatchOT) {
        dao.deleteAllContainerByOtId(it.otNumber)
        dao.updateOT(it)
        it.containers?.let {
            //dao.insertOrReplaceContainers(it)
            it.forEach { container ->
                container.bales?.let { bales ->
                    dao.insertOrReplaceBales(bales)
                }

                dao.insertOrReplaceContainer(container)
            }
        }
    }

    override fun getOTById(otNumber: String) = dao.getOTByIdLocal(otNumber)

    override fun loadContainerAndBales(containerNo: String, otNumber: String) =
        dao.loadContainerAndBales(containerNo, otNumber)

    override suspend fun getOtList(): LiveData<Resource<GenericReqAndResp<List<DispatchOT>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<DispatchOT>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<DispatchOT>> =
                api.getOTList(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun insertOrReplaceOTs(it: List<DispatchOT>) {
        dao.deleteAllOTs()
        dao.insertOrReplaceOTs(it)
    }

    override fun getOTsLocal(): List<DispatchOT> = dao.getOTsLocal()
    override suspend fun deleteContainerFromServer(
        containerNumber: String,
        otNumber: String
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall(): GenericReqAndResp<GenericMessage> =
                api.deleteContainer(
                    getCurrentKey(),
                    containerNumber = containerNumber,
                    otNumber = otNumber
                )
        }.build().asLiveData()
    }

    override fun getContainerAndBale(containerId: String, otNumber: String) =
        dao.loadContainerAndBale(containerId, otNumber)

    override fun deleteContainer(containerId: String) {
        dao.deleteContainerById(containerId)
        dao.deleteBaleByContainerId(containerId)
    }

    override suspend fun holdStuffing(
        containerNumber: String,
        otNumber: String,
        status: Int
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall(): GenericReqAndResp<GenericMessage> =
                api.holdStuffing(
                    getCurrentKey(),
                    ContainerNumber = containerNumber,
                    status = status,
                    otNumber = otNumber
                )
        }.build().asLiveData()
    }

    override suspend fun updateDispatchMode(
        otNumber: String,
        isDirect: Boolean
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall(): GenericReqAndResp<GenericMessage> =
                api.updateDispatchMode(getCurrentKey(), isDirect, !isDirect, otNumber)
        }.build().asLiveData()
    }

    override suspend fun getBaleDetails(
        baleId: String,
        containerId: String,
        otNumber: String?,
        isDirect: Boolean
    ): LiveData<Resource<GenericReqAndResp<PortBale>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<PortBale>>() {
            override suspend fun createCall(): GenericReqAndResp<PortBale> =
                api.getBaleDetail(
                    getCurrentKey(),
                    isDirect, PortBale(
                        baleId = baleId,
                        containerNumber = containerId,
                        otNumber = otNumber
                    )
                )
        }.build().asLiveData()
    }

    override suspend fun insertOrReplaceBale(bale: PortBale) = dao.insertOrReplaceBale(bale)

    /*override suspend fun getBaleDetails(
         baleId: String,
         containerId: String,
         otNumber: String?,
         isDirect: Boolean
     ): LiveData<Resource<PortBale>> {
         return object : NetworkBoundResource<PortBale, GenericReqAndResp<PortBale>>() {
             override fun processResponse(response: GenericReqAndResp<PortBale>):
                     PortBale = response.data

             override suspend fun saveCallResults(items: PortBale) {
                 dao.insertOrReplaceBale(items)
             }

             override fun shouldFetch(data: PortBale?): Boolean = true

             override suspend fun loadFromDb(): PortBale = dao.getBaleDeatils(baleId)

             override suspend fun createCall(): GenericReqAndResp<PortBale> =
                 api.getBaleDetail(
                     isDirect, PortBale(
                         baleId = baleId,
                         containerNumber = containerId,
                         otNumber = otNumber
                     )
                 )

         }.build().asLiveData()
     }
 */
    override suspend fun deleteBaleDetail(
        baleId: String,
        containerNumber: String
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall(): GenericReqAndResp<GenericMessage> =
                api.deleteBaleDetail(getCurrentKey(), baleId)
        }.build().asLiveData()
    }

    override fun deleteBaleFromDB(bale: PortBale) = dao.deleteBale(bale)
    override suspend fun sealContainer(
        sealId: String,
        containerId: String,
        otNumber: String,
        direct: Boolean
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall(): GenericReqAndResp<GenericMessage> =
                api.sealContainer(getCurrentKey(), sealId, containerId, otNumber, direct)
        }.build().asLiveData()
    }

    override fun updateSealIDToLocalDB(sealNumber: String, containerNumber: String) =
        dao.updateSealNumber(sealNumber, containerNumber)

    override suspend fun getOTDetailCom(otNumber: String): LiveData<Resource<GenericReqAndResp<DispatchOT>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<DispatchOT>>() {
            override suspend fun createCall(): GenericReqAndResp<DispatchOT> =
                api.getOTDetails(getCurrentKey(), otNumber)
        }.build().asLiveData()
    }

    override suspend fun confirmDispatch(otNumber: String): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall(): GenericReqAndResp<GenericMessage> =
                api.confirmDispatch(getCurrentKey(), otNumber)
        }.build().asLiveData()
    }

    override suspend fun doBreakSealContainer(
        otNumber: String,
        containerNumber: String,
        sealNumber: String
    ): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall(): GenericReqAndResp<GenericMessage> =
                api.brakSealContainer(getCurrentKey(), otNumber, containerNumber, sealNumber)
        }.build().asLiveData()
    }

    override fun updateOTNumberLocalDB(otNumber: String, containerNumber: String) =
            dao.updateOTNumber(otNumber, containerNumber)

    override suspend fun updateOTNumberDispatch(otNumber: String, oldOTNumber: String, containerList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<SplitContainer>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<SplitContainer>>() {
            override suspend fun createCall(): GenericReqAndResp<SplitContainer> =
                api.updateOTNumberDispatch(getCurrentKey(), otNumber, oldOTNumber, containerList)
        }.build().asLiveData()
    }
    override fun updateSplitContainer(isSelected: Boolean, containerNumber: String) =
            dao.updateSplitContainer(isSelected, containerNumber)
}
