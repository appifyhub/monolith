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

from appifyhub.models.token_response import TokenResponse

class TestTokenResponse(unittest.TestCase):
    """TokenResponse unit test stubs"""

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def make_instance(self, include_optional) -> TokenResponse:
        """Test TokenResponse
            include_option is a boolean, when False only required
            params are included, when True both required and
            optional params are included """
        # uncomment below to create an instance of `TokenResponse`
        """
        model = TokenResponse()
        if include_optional:
            return TokenResponse(
                token_value = ''
            )
        else:
            return TokenResponse(
                token_value = '',
        )
        """

    def testTokenResponse(self):
        """Test TokenResponse"""
        # inst_req_only = self.make_instance(include_optional=False)
        # inst_req_and_optional = self.make_instance(include_optional=True)

if __name__ == '__main__':
    unittest.main()