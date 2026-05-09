package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.CreateSpaceRequest
import com.app.zonetask.data.remote.dto.EditSpaceRequest
import com.app.zonetask.data.remote.dto.SpaceMemberResponse
import com.app.zonetask.data.remote.dto.SpacePermissionsResponse
import com.app.zonetask.data.remote.dto.SpaceResponse
import com.app.zonetask.data.remote.dto.UpdateMemberRoleRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface SpaceApiService {

    @GET(AppConstants.Api.Paths.USER_SPACES)
    suspend fun getSpacesByUser(
        @Path("userId") userId: Int
    ): Response<List<SpaceResponse>>

    @GET(AppConstants.Api.Paths.SPACE_BY_ID)
    suspend fun getSpaceById(
        @Path("spaceId") spaceId: Int
    ): Response<SpaceResponse>

    @POST(AppConstants.Api.Paths.CREATE_SPACE)
    suspend fun createSpace(
        @Body request: CreateSpaceRequest
    ): Response<SpaceResponse>

    @PUT(AppConstants.Api.Paths.UPDATE_SPACE)
    suspend fun updateSpace(
        @Path("spaceId") spaceId: Int,
        @Body request: EditSpaceRequest
    ): Response<SpaceResponse>

    @DELETE(AppConstants.Api.Paths.DELETE_SPACE)
    suspend fun deleteSpace(
        @Path("spaceId") spaceId: Int,
        @Query("userId") userId: Int
    ): Response<Unit>

    @GET(AppConstants.Api.Paths.SPACE_PERMISSIONS)
    suspend fun getSpacePermissions(
        @Path("spaceId") spaceId: Int,
        @Query("userId") userId: Int
    ): Response<SpacePermissionsResponse>

    @GET(AppConstants.Api.Paths.SPACE_MEMBERS)
    suspend fun getSpaceMembers(
        @Path("spaceId") spaceId: Int,
        @Query("userId") userId: Int
    ): Response<List<SpaceMemberResponse>>

    @PUT(AppConstants.Api.Paths.UPDATE_MEMBER_ROLE)
    suspend fun updateMemberRole(
        @Path("spaceId") spaceId: Int,
        @Path("memberId") memberId: Int,
        @Body request: UpdateMemberRoleRequest
    ): Response<SpaceMemberResponse>
}
