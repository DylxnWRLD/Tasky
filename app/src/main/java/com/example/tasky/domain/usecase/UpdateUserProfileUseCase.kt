package com.example.tasky.domain.usecase

import com.example.tasky.domain.repository.UserRepository

class UpdateUserProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        name: String? = null,
        location: String? = null,
        experience: String? = null,
        bio: String? = null,
        skills: List<String>? = null,
        profileImage: String? = null
    ): Result<Unit> {
        return userRepository.updateUserProfile(
            userId = userId,
            name = name,
            location = location,
            experience = experience,
            bio = bio,
            skills = skills,
            profileImage = profileImage
        )
    }
}