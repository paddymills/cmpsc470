# Define the grammar for the language
grammar = {
    'E': [('E', '+', 'T'), ('T',)],
    'T': [('T', '*', 'F'), ('F',)],
    'F': [('(', 'E', ')'), ('id',)]
}

# Define the LR parsing table
parsing_table = {
    (0, '+'): ('shift', 2),
    (0, '*'): ('shift', 3),
    (0, '('): ('shift', 4),
    (0, 'id'): ('shift', 5),
    (2, '+'): ('shift', 2),
    (2, '*'): ('shift', 3),
    (2, ')'): ('reduce', 2),
    (2, '$'): ('reduce', 2),
    (3, '+'): ('reduce', 1),
    (3, '*'): ('shift', 3),
    (3, ')'): ('reduce', 1),
    (3, '$'): ('reduce', 1),
    (4, '+'): ('shift', 2),
    (4, '*'): ('shift', 3),
    (4, '('): ('shift', 4),
    (4, 'id'): ('shift', 5),
    (5, '+'): ('reduce', 3),
    (5, '*'): ('reduce', 3),
    (5, ')'): ('reduce', 3),
    (5, '$'): ('reduce', 3)
}

def lr_parser(input_string):
    # Initialize the stack with the start symbol and state 0
    stack = [(0, 'E')]

    # Convert the input string to a list of tokens
    tokens = input_string.split()

    # Add the end-of-file marker to the end of the input
    tokens.append('$')
    print("Tokens:", tokens)

    # Process each token
    while len(stack) > 0:
        print("---\nStack:", stack)
        # Get the current state and symbol from the top of the stack
        state, symbol = stack[-1]

        # Get the next token from the input
        if len(tokens) > 0:
            token = tokens[0]
        else:
            token = '$'

        # Check if there is a valid entry in the parsing table
        if (state, token) in parsing_table:
            action, value = parsing_table[(state, token)]
            if action == 'shift':
                # Shift the token onto the stack and update the state
                tokens = tokens[1:]
                stack.append((value, token))
            elif action == 'reduce':
                # Reduce the stack by popping the symbols and state
                production = grammar[symbol][value]
                lhs, rhs = production[0], production[1:]
                stack = stack[:-len(rhs)]
                state, _ = stack[-1]
                stack.append((state, lhs))


to_parse = "id * id + id"
lr_parser(to_parse)
