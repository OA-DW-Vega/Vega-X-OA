package com.olam.warehouse.master.work

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.olam.warehouse.presentation.utils.constraint
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

const val FILE_PATH = "file_path"
const val LOCAL_PATH = "local_path"
const val DIRECTORY_PATH = "dir_path"

fun getReceivingOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaReceivingPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getQualityOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaQualityPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getMtnrQualityOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaGhanaMtnrQualityPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getMtntOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaMtntPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getDispatchOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaDispatchPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getRminOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaRminPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

//Cocoa

fun getDispatchMtntOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaCocoaDispatchMtntPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

//Ecuador

fun getOffloadingOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaEcuadorOffloadingPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getGrnOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaGrnPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

//Ghana Cocoa

fun getGhanaCocoaOffloadingOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaGhanaCocoaOffloadingGrnPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

//Ghana

fun getFgrnOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaFgrnPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getVirtualOneTimeRequestWorker(data: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaCocoaVirtualPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getInputData(downloadFilePath: String, directory: String, localPath: String): Data {
    return Data.Builder().putString(FILE_PATH, downloadFilePath).putString(LOCAL_PATH, localPath)
        .putString(DIRECTORY_PATH, directory).build()
}

fun getOneTimeRequest(inputData: Data): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(FileDownloadWorker::class.java)
        .setConstraints(constraint)
        .setInputData(inputData).build()
}

fun enQueueUniqueWorker(worker: OneTimeWorkRequest) {
    WorkManager.getInstance().enqueueUniqueWork(Random.nextInt().toString(), ExistingWorkPolicy.KEEP, worker)
}

fun getFeatureReleaseOneTimeRequestWorker(data: Data, tag: String): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(AppCenterReleaseIdWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getFeatureReleaseUrlOneTimeRequestWorker(data: Data, tag: String): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(AppCenterReleaseUrlWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(data).build()
}

fun getEcuadorDispatchOneTimeRequestWorker(deliveryId: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaEcuadorDispatchPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(deliveryId).build()
}

fun getGhanaMTNTDispatchOneTimeRequestWorker(deliveryId: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaGhanaDispatchPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(deliveryId).build()
}

fun getGhanaCocoaMTNTDispatchOneTimeRequestWorker(deliveryId: Data, tag: Int): OneTimeWorkRequest {
    return OneTimeWorkRequest.Builder(VegaGhanaCocoaDispatchPostWorker::class.java).addTag(tag.toString())
        .setConstraints(constraint).setInputData(deliveryId).build()
}

