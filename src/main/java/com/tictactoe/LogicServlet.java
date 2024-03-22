package com.tictactoe;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Getting the current session
        HttpSession currentSession = req.getSession();

        //Getting the object of the playing field from the session
        Field field = extractField(currentSession);

        //Getting the index of the clicked cell
        int index = getSelectedIndex(req);
        Sign currentSign = field.getField().get(index);

        // Check that the clicked cell is empty.
        // Otherwise, do nothing and send the user to the same page without changes
        // parameters in the session
        if (Sign.EMPTY != currentSign) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }

        //Put a cross in the cell that the user clicked on
        field.getField().put(index, Sign.CROSS);

        //Check whether the cross won after adding the user's last click
        if (checkWin(resp, currentSession, field)) {
            return;
        }

        //Getting an empty field cell
        int emptyFieldIndex = field.getEmptyFieldIndex();

        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            //Check if a NOUGHT won after adding the last NOUGHT
            if (checkWin(resp, currentSession, field)) {
                return;
            }
        } else
        //If there is no empty cell and no one has won, it is a draw
            {
                // Add a flag to the session that signals that a tie has occurred
                currentSession.setAttribute("draw", true);

                // Counting the list of signs
                List<Sign> data = field.getFieldData();

                // Updating this list in the session
                currentSession.setAttribute("data", data);

                //Sending the redirect
                resp.sendRedirect("/index.jsp");
                return;
        }

        //Counting the list of signs
        List<Sign> data = field.getFieldData();

        //Updating the field object and the list of signs in the session
        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        resp.sendRedirect("/index.jsp");
    }

    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }

   /**
    * The method checks whether there are no three crosses/zeros in one row.
    * Returns true/false
    */
   private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
       Sign winner = field.checkWin();
       if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
           // Adding a flag that shows that someone has won
           currentSession.setAttribute("winner", winner);

           // Counting the list of signs
           List<Sign> data = field.getFieldData();

           // Updating this list in the session
           currentSession.setAttribute("data", data);

           // Sending the redirect
           response.sendRedirect("/index.jsp");
           return true;
       }
       return false;
   }

}
