/**
 * Fetch methodes for secret api calls
 * @author Peter Rutschmann
 */

const getApiUrl = () =>
    `${process.env.REACT_APP_API_PROTOCOL}://${process.env.REACT_APP_API_HOST}${process.env.REACT_APP_API_PORT ? `:${process.env.REACT_APP_API_PORT}` : ''}${process.env.REACT_APP_API_PATH}`;

//Post secret to server
// Needs loginValues.token (set after login, see LoginUser.js) - the
// backend uses it to know which user is calling instead of trusting
// the email in the request body.
export const postSecret = async ({ loginValues, content }) => {

    const API_URL = getApiUrl();

    try {
        const response = await fetch(`${API_URL}/secrets`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${loginValues.token}`
            },
            body: JSON.stringify({
                email: loginValues.email,
                content: content,
                encryptPassword: loginValues.password
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Server response failed.');
        }

        return await response.json();
    } catch (error) {
        console.error('Error posting secret:', error.message);
        throw new Error('Failed to save secret. ' + error.message);
    }
};

//get all secrets for the logged in user
export const getSecretsforUser = async (loginValues) => {

    const API_URL = getApiUrl();

    try {
        const response = await fetch(`${API_URL}/secrets/byemail`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${loginValues.token}`
            },
            body: JSON.stringify({
                email: loginValues.email,
                encryptPassword: loginValues.password
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Server response failed.');
        }

        return await response.json();

    } catch (error) {
        console.error('Failed to get secrets:', error.message);
        throw new Error('Failed to get secrets. ' + error.message);
    }
};
