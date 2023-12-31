package org.eclipse.paho.android.service;

/**
 * Enumeration representing the success or failure of an operation
 */
enum Status {
    /**
     * Indicates that the operation succeeded
     */
    OK,

    /**
     * Indicates that the operation failed
     */
    ERROR,

    /**
     * Indicates that the operation's result may be returned asynchronously
     */
    NO_RESULT
}
