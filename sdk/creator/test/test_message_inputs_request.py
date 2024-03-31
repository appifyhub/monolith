# coding: utf-8

"""
    Appify Hub's Creator API

    The full specification of the service's API used by the project administrators.

    The version of the OpenAPI document: Latest
    Contact: contact@appifyhub.com
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import unittest

from appifyhub.models.message_inputs_request import MessageInputsRequest

class TestMessageInputsRequest(unittest.TestCase):
    """MessageInputsRequest unit test stubs"""

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def make_instance(self, include_optional) -> MessageInputsRequest:
        """Test MessageInputsRequest
            include_option is a boolean, when False only required
            params are included, when True both required and
            optional params are included """
        # uncomment below to create an instance of `MessageInputsRequest`
        """
        model = MessageInputsRequest()
        if include_optional:
            return MessageInputsRequest(
                user_id = '',
                project_id = 56
            )
        else:
            return MessageInputsRequest(
        )
        """

    def testMessageInputsRequest(self):
        """Test MessageInputsRequest"""
        # inst_req_only = self.make_instance(include_optional=False)
        # inst_req_and_optional = self.make_instance(include_optional=True)

if __name__ == '__main__':
    unittest.main()
