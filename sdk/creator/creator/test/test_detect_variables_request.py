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

from appifyhub.models.detect_variables_request import DetectVariablesRequest

class TestDetectVariablesRequest(unittest.TestCase):
    """DetectVariablesRequest unit test stubs"""

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def make_instance(self, include_optional) -> DetectVariablesRequest:
        """Test DetectVariablesRequest
            include_option is a boolean, when False only required
            params are included, when True both required and
            optional params are included """
        # uncomment below to create an instance of `DetectVariablesRequest`
        """
        model = DetectVariablesRequest()
        if include_optional:
            return DetectVariablesRequest(
                content = ''
            )
        else:
            return DetectVariablesRequest(
                content = '',
        )
        """

    def testDetectVariablesRequest(self):
        """Test DetectVariablesRequest"""
        # inst_req_only = self.make_instance(include_optional=False)
        # inst_req_and_optional = self.make_instance(include_optional=True)

if __name__ == '__main__':
    unittest.main()
