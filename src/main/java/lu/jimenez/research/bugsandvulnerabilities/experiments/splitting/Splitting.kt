/////////////////////////////////////////////////////////////////////////////////////////
//                 University of Luxembourg  -
//                 Interdisciplinary center for Security and Trust (SnT)
//                 Copyright © 2016 University of Luxembourg, SnT
//
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 3 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//
//
//    Author: Matthieu Jimenez – SnT – matthieu.jimenez@uni.lu
//
//////////////////////////////////////////////////////////////////////////////////////////
package lu.jimenez.research.bugsandvulnerabilities.experiments.splitting

import lu.jimenez.research.bugsandvulnerabilities.experiments.splitting.kfoldxvalidation.KFold
import lu.jimenez.research.bugsandvulnerabilities.experiments.splitting.random.Random
import lu.jimenez.research.bugsandvulnerabilities.experiments.splitting.time.Time
import lu.jimenez.research.bugsandvulnerabilities.experiments.splitting.utils.Constants
import lu.jimenez.research.bugsandvulnerabilities.model.extension.experiment.ExperimentalSets
import lu.jimenez.research.bugsandvulnerabilities.model.internal.Document
import lu.jimenez.research.bugsandvulnerabilities.model.internal.DocumentType
import lu.jimenez.research.bugsandvulnerabilities.utils.Serialization
import java.util.*


class Splitting(val pathOfLoading:String, val pathOfSaving:String, val choice:String) {
    val mapOfIdType = Serialization.loadMapHashData(pathOfLoading+"${choice}_MapOfIdCat.obj") as Map<Int,DocumentType>

    fun launchexperiment() {
        val kfold = launchKfold()
        Serialization.saveListData(kfold,pathOfSaving+"${choice}_listKfold.obj")
        if (Constants.TIME) {
            val time = launchTime(kfold)
            Serialization.saveMapStringData(time,pathOfSaving+"${choice}_MapOfIdTime.obj")
        }
        if (Constants.RANDOM) {
            launchRandom(kfold)
        }
    }

    fun launchKfold(): List<Int> {
        return KFold.computeKfoldVulnBugClear(mapOfIdType)
    }

    fun launchRandom(idFile :List<Int>) {
        val rand = Random(idFile)
        val pureRand = rand.pureRandom()
        Serialization.saveMapStringData(pureRand,pathOfSaving+"${choice}_MapOfPureRandom.obj")
        val equirand = rand.equilibrateRandom(mapOfIdType)
        Serialization.saveMapStringData(equirand,pathOfSaving+"${choice}_MapOfEquiRandom.obj")
    }

    fun launchTime(idFile : List<Int>): Map<String, ExperimentalSets> {
        val mapOfIdDoc = Serialization.loadMapHashData(pathOfLoading+"${choice}_MapOfIdDoc.obj") as Map<Int, Document>
        return Time.splitting(idFile,mapOfIdDoc)
    }


    companion object run {

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size > 0 && args.size < 3) {
                val pathOfLoading = args[0]
                val pathOfSaving: String
                if (args.size == 2)
                    pathOfSaving = args[1]
                else pathOfSaving = pathOfLoading

                loadingProperties()
                if(Constants.EXPERIMENTAL_GEN)
                Splitting(pathOfLoading,pathOfSaving,"experimental").launchexperiment()
                if(Constants.REALISTIC_GEN)
                Splitting(pathOfLoading,pathOfSaving,"real").launchexperiment()
            }
        }
        /**
         * Method for loading properties located in the collector.properties file
         */
        fun loadingProperties() {
            val inputStream = this.javaClass.classLoader.getResourceAsStream("collector.properties")

            val properties = Properties()
            properties.load(inputStream)
            inputStream.close()
            Constants.REALISTIC_GEN = properties.getProperty("realisticgen").toBoolean()
            Constants.EXPERIMENTAL_GEN = properties.getProperty("experimentalgen").toBoolean()
            Constants.TIME_SPLIT = properties.getProperty("timeSplit").split(",").map { time -> time.toInt() }
            Constants.NB_EXPERIMENT_RANDOM_PURE = properties.getProperty("nbRandomPureExperiment").toInt()
            Constants.NB_EXPERIMENT_EQUILIBRATE = properties.getProperty("nbRandomEquilibrateExperiment").toInt()
            Constants.RANDOM = properties.getProperty("random").toBoolean()
            Constants.TIME = properties.getProperty("time").toBoolean()

        }
    }

}