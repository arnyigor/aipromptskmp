package com.arny.aiprompts.models

import kotlinx.serialization.Serializable

@Serializable
data class GitHubCommitResponse(val sha: String)