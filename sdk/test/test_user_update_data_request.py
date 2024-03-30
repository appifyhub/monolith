# coding: utf-8

"""
    Appify Hub's Consumer API

    The full specification of the service's API used by the end-users.

    The version of the OpenAPI document: Latest
    Contact: contact@appifyhub.com
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import unittest

from appifyhub.models.user_update_data_request import UserUpdateDataRequest

class TestUserUpdateDataRequest(unittest.TestCase):
    """UserUpdateDataRequest unit test stubs"""

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def make_instance(self, include_optional) -> UserUpdateDataRequest:
        """Test UserUpdateDataRequest
            include_option is a boolean, when False only required
            params are included, when True both required and
            optional params are included """
        # uncomment below to create an instance of `UserUpdateDataRequest`
        """
        model = UserUpdateDataRequest()
        if include_optional:
            return UserUpdateDataRequest(
                name = appifyhub.models.settable_request.SettableRequest(
                    value = appifyhub.models.value.value(), ),
                type = appifyhub.models.settable_request.SettableRequest(
                    value = appifyhub.models.value.value(), ),
                allows_spam = appifyhub.models.settable_request.SettableRequest(
                    value = appifyhub.models.value.value(), ),
                contact = appifyhub.models.settable_request.SettableRequest(
                    value = appifyhub.models.value.value(), ),
                contact_type = appifyhub.models.settable_request.SettableRequest(
                    value = appifyhub.models.value.value(), ),
                birthday = appifyhub.models.settable_request.SettableRequest(
                    value = appifyhub.models.value.value(), ),
                company = appifyhub.models.organization_updater_settable.OrganizationUpdaterSettable(
                    value = appifyhub.models.organization_updater_dto.OrganizationUpdaterDto(
                        name = appifyhub.models.settable_request.SettableRequest(
                            value = appifyhub.models.value.value(), ), 
                        street = appifyhub.models.settable_request.SettableRequest(
                            value = appifyhub.models.value.value(), ), 
                        postcode = , 
                        city = , 
                        country_code = , ), ),
                language_tag = appifyhub.models.settable_request.SettableRequest(
                    value = appifyhub.models.value.value(), )
            )
        else:
            return UserUpdateDataRequest(
        )
        """

    def testUserUpdateDataRequest(self):
        """Test UserUpdateDataRequest"""
        # inst_req_only = self.make_instance(include_optional=False)
        # inst_req_and_optional = self.make_instance(include_optional=True)

if __name__ == '__main__':
    unittest.main()