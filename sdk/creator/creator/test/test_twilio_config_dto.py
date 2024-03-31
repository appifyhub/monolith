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

from appifyhub.models.twilio_config_dto import TwilioConfigDto

class TestTwilioConfigDto(unittest.TestCase):
    """TwilioConfigDto unit test stubs"""

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def make_instance(self, include_optional) -> TwilioConfigDto:
        """Test TwilioConfigDto
            include_option is a boolean, when False only required
            params are included, when True both required and
            optional params are included """
        # uncomment below to create an instance of `TwilioConfigDto`
        """
        model = TwilioConfigDto()
        if include_optional:
            return TwilioConfigDto(
                account_sid = '',
                auth_token = '',
                messaging_service_id = '',
                max_price_per_message = 56,
                max_retry_attempts = 56,
                default_sender_name = '',
                default_sender_number = ''
            )
        else:
            return TwilioConfigDto(
                account_sid = '',
                auth_token = '',
                messaging_service_id = '',
                max_price_per_message = 56,
                max_retry_attempts = 56,
                default_sender_name = '',
                default_sender_number = '',
        )
        """

    def testTwilioConfigDto(self):
        """Test TwilioConfigDto"""
        # inst_req_only = self.make_instance(include_optional=False)
        # inst_req_and_optional = self.make_instance(include_optional=True)

if __name__ == '__main__':
    unittest.main()