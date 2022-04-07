package com.iconloop.score.example.utils;
import score.Address;
import score.annotation.EventLog;


public class Eventlogs {
    @EventLog(indexed = 1)
    public static void FundReturned(Address _sponsor_address, String note) {
    }

    @EventLog(indexed = 1)
    public static void ProposalFundTransferred(String _ipfs_key, String note) {
    }

    @EventLog(indexed = 1)
    public static void ProposalDisqualified(String _ipfs_key, String note) {
    }

    @EventLog(indexed = 1)
    public static void FundReceived(Address _sponsor_address, String note) {
    }
}
