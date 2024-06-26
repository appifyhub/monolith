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

from appifyhub.models.user_update_authority_request import UserUpdateAuthorityRequest

class TestUserUpdateAuthorityRequest(unittest.TestCase):
    """UserUpdateAuthorityRequest unit test stubs"""

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def make_instance(self, include_optional) -> UserUpdateAuthorityRequest:
        """Test UserUpdateAuthorityRequest
            include_option is a boolean, when False only required
            params are included, when True both required and
            optional params are included """
        # uncomment below to create an instance of `UserUpdateAuthorityRequest`
        """
        model = UserUpdateAuthorityRequest()
        if include_optional:
            return UserUpdateAuthorityRequest(
                authority = 'DEFAULT'
            )
        else:
            return UserUpdateAuthorityRequest(
                authority = 'DEFAULT',
        )
        """

    def testUserUpdateAuthorityRequest(self):
        """Test UserUpdateAuthorityRequest"""
        # inst_req_only = self.make_instance(include_optional=False)
        # inst_req_and_optional = self.make_instance(include_optional=True)

if __name__ == '__main__':
    unittest.main()
