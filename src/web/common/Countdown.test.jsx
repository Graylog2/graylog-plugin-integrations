import React from 'react';
// import ReactDOM from 'react-dom';
import {
  render,
  fireEvent,
  getByTestId,
  rerender,
} from '@testing-library/react';
import Countdown from './Countdown';

it('Countdown loads with initial state of 0', () => {
  const { container } = render(<Countdown timeInSeconds={2} />);
  const countdownValue = getByTestId(container, 'countdown');
  expect(countdownValue.textContent).toBe('00:02');
});

// it('Increment and decrement buttons work', () => {
//   const { container } = render(<Countdown />);
//   const countValue = getByTestId(container, 'countvalue');
//   const increment = getByTestId(container, 'incrementButton');
//   const decrement = getByTestId(container, 'decrementButton');

//   expect(countValue.textContent).toBe('0');

//   fireEvent.click(increment);
//   expect(countValue.textContent).toBe('1');
//   fireEvent.click(decrement);
//   expect(countValue.textContent).toBe('0');
// });

// it('Submitting a name via the input field changes the name state value', () => {
//   const { container, rerender } = render(<Countdown />);
//   const nameValue = getByTestId(container, 'namevalue');
//   const inputName = getByTestId(container, 'inputName');

//   const submitButton = getByTestId(container, 'submitRefButton');
//   const newName = 'Ben';

//   fireEvent.change(inputName, { target: { value: newName } });
//   fireEvent.click(submitButton);
//   expect(nameValue.textContent).toEqual(newName);

//   rerender(<Countdown />);
//   expect(window.localStorage.getItem('name')).toBe(newName);
// });
