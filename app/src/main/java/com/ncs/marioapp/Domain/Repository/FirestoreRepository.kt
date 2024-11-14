package com.ncs.marioapp.Domain.Repository

import com.ncs.marioapp.Domain.Models.Admin.RoundQuestionnaire
import com.ncs.marioapp.Domain.Models.Admin.Round
import com.ncs.marioapp.Domain.Models.Events.EventDetails.Submission
import com.ncs.marioapp.Domain.Models.ServerResult

interface FirestoreRepository {
    suspend fun postQuestionnaire(questionnaire: RoundQuestionnaire, callback: (ServerResult<Boolean>) -> Unit)
    suspend fun postRound(round: Round, callback: (ServerResult<Boolean>) -> Unit)
    suspend fun getRounds(eventID: String, callback: (ServerResult<List<Round>>) -> Unit)
    suspend fun getQuestionnaire(quiestionnaireID: String, callback: (ServerResult<RoundQuestionnaire>) -> Unit)
    suspend fun postSubmission(submission: Submission, callback: (ServerResult<Boolean>) -> Unit)
    suspend fun getSubmissions(eventId: String, userId:String ,callback: (ServerResult<List<Submission>>) -> Unit)
}